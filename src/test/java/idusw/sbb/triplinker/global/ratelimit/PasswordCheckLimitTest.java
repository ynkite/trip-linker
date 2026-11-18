package idusw.sbb.triplinker.global.ratelimit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * KISA 2-20 자동화 공격 — 로그인한 상태에서 비밀번호를 확인하는 주소(현재 비밀번호 확인 · 비밀번호 변경 · 탈퇴)의 횟수 제한.
 * 로그인은 5번 틀리면 5분 잠기지만, 이 주소들은 제한이 없어 토큰만 있으면 비밀번호를 계속 넣어 볼 수 있었다.
 */
class PasswordCheckLimitTest {

    @RestController
    static class Stub {
        @PostMapping("/api/users/me/verify-password") ResponseEntity<String> verify() { return ResponseEntity.ok("ok"); }
        @PatchMapping("/api/users/me/password") ResponseEntity<String> change() { return ResponseEntity.ok("ok"); }
        @DeleteMapping("/api/users/me") ResponseEntity<String> withdraw() { return ResponseEntity.ok("ok"); }
        @GetMapping("/api/users/me") ResponseEntity<String> read() { return ResponseEntity.ok("ok"); }
        @PatchMapping("/api/users/me") ResponseEntity<String> profile() { return ResponseEntity.ok("ok"); }
    }

    final AtomicLong now = new AtomicLong(1_000_000L);
    final MockMvc mvc = MockMvcBuilders.standaloneSetup(new Stub())
            .addInterceptors(new PasswordCheckLimitInterceptor(now::get)).build();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("비밀번호를 확인하는 세 주소는 합쳐서 한 회원이 10분에 10번까지, 11번째는 429")
    void limitAcrossPasswordChecks() throws Exception {
        idusw.sbb.triplinker.security.TripFixtureAccess.loginAs(1L);
        for (int i = 0; i < 8; i++) mvc.perform(post("/api/users/me/verify-password")).andExpect(status().isOk());
        mvc.perform(patch("/api/users/me/password")).andExpect(status().isOk());
        mvc.perform(delete("/api/users/me")).andExpect(status().isOk());
        mvc.perform(post("/api/users/me/verify-password")).andExpect(status().isTooManyRequests());
        mvc.perform(delete("/api/users/me")).andExpect(status().isTooManyRequests());
        // 다른 회원은 따로 센다
        idusw.sbb.triplinker.security.TripFixtureAccess.loginAs(2L);
        mvc.perform(post("/api/users/me/verify-password")).andExpect(status().isOk());
        // 10분이 지나면 다시 된다
        idusw.sbb.triplinker.security.TripFixtureAccess.loginAs(1L);
        now.addAndGet(10 * 60 * 1000L + 1);
        mvc.perform(post("/api/users/me/verify-password")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("정상: 내 정보 읽기 · 프로필 수정은 세지 않는다")
    void otherCallsAreNotCounted() throws Exception {
        idusw.sbb.triplinker.security.TripFixtureAccess.loginAs(1L);
        for (int i = 0; i < 30; i++) {
            mvc.perform(get("/api/users/me")).andExpect(status().isOk());
            mvc.perform(patch("/api/users/me")).andExpect(status().isOk());
        }
        mvc.perform(post("/api/users/me/verify-password")).andExpect(status().isOk());
    }
}
