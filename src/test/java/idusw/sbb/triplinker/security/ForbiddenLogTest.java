package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.global.exception.ForbiddenException;
import idusw.sbb.triplinker.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** OWASP A09 — 남의 데이터에 손대다 막힌 요청(403)이 서버 로그에 남는가. 전에는 응답만 하고 기록이 없었다. */
@ExtendWith(OutputCaptureExtension.class)
class ForbiddenLogTest {

    @RestController
    static class Stub {
        @GetMapping("/api/trips/10/expenses")
        ResponseEntity<String> read() { throw new ForbiddenException(); }
    }

    final MockMvc mvc = MockMvcBuilders.standaloneSetup(new Stub())
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("권한 거부는 403 으로 답하고, 주소와 회원 번호를 서버 로그에 남긴다")
    void forbiddenIsLogged(CapturedOutput out) throws Exception {
        TripFixture.loginAs(2L);
        mvc.perform(get("/api/trips/10/expenses")).andExpect(status().isForbidden());
        assertThat(out.getAll()).contains("[권한 거부]").contains("/api/trips/10/expenses").contains("user=2");
    }
}
