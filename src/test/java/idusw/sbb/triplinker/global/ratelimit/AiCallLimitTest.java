package idusw.sbb.triplinker.global.ratelimit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** OWASP LLM「무제한 소비」 — AI 를 부르는 주소의 회원별 호출 횟수 제한. */
class AiCallLimitTest {

    @RestController
    static class Stub {
        @PostMapping("/api/chat/message") ResponseEntity<String> chat() { return ResponseEntity.ok("ok"); }
        @PostMapping("/api/trips/10/routes/generate") ResponseEntity<String> gen() { return ResponseEntity.ok("ok"); }
        @PostMapping("/api/trips/10/routes/reorder") ResponseEntity<String> reorder() { return ResponseEntity.ok("ok"); }
        @GetMapping("/api/trips/10/routes") ResponseEntity<String> read() { return ResponseEntity.ok("ok"); }
    }

    final AtomicLong now = new AtomicLong(1_000_000L);
    final MockMvc mvc = MockMvcBuilders.standaloneSetup(new Stub())
            .addInterceptors(new AiCallLimitInterceptor(now::get)).build();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("챗봇은 한 회원이 10분에 40번까지, 41번째는 429")
    void chatLimit() throws Exception {
        idusw.sbb.triplinker.security.TripFixtureAccess.loginAs(1L);
        for (int i = 0; i < 40; i++) mvc.perform(post("/api/chat/message")).andExpect(status().isOk());
        mvc.perform(post("/api/chat/message")).andExpect(status().isTooManyRequests());
        // 다른 회원은 따로 센다
        idusw.sbb.triplinker.security.TripFixtureAccess.loginAs(2L);
        mvc.perform(post("/api/chat/message")).andExpect(status().isOk());
        // 10분이 지나면 다시 된다
        idusw.sbb.triplinker.security.TripFixtureAccess.loginAs(1L);
        now.addAndGet(10 * 60 * 1000L + 1);
        mvc.perform(post("/api/chat/message")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("일정 생성 · 교체는 1시간에 30번, 순서 저장 · 일정 읽기는 제한 없음")
    void routeLimit() throws Exception {
        idusw.sbb.triplinker.security.TripFixtureAccess.loginAs(1L);
        for (int i = 0; i < 30; i++) mvc.perform(post("/api/trips/10/routes/generate")).andExpect(status().isOk());
        mvc.perform(post("/api/trips/10/routes/generate")).andExpect(status().isTooManyRequests());
        for (int i = 0; i < 50; i++) {
            mvc.perform(post("/api/trips/10/routes/reorder")).andExpect(status().isOk());
            mvc.perform(get("/api/trips/10/routes")).andExpect(status().isOk());
        }
    }
}
