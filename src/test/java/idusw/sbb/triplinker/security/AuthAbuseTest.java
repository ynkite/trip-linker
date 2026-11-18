package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.domain.auth.controller.AuthController;
import idusw.sbb.triplinker.domain.auth.dto.SignUpRequestDTO;
import idusw.sbb.triplinker.domain.auth.entity.EmailAuth;
import idusw.sbb.triplinker.domain.auth.repository.EmailAuthRepository;
import idusw.sbb.triplinker.domain.auth.service.AuthService;
import idusw.sbb.triplinker.domain.auth.service.EmailAuthService;
import idusw.sbb.triplinker.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OWASP A07(식별 · 인증 실패) 재현 시험 — 이메일 인증번호(6자리, 3분) 를 쓰는 비밀번호 재설정 · 회원가입.
 */
class AuthAbuseTest {

    static final String EMAIL = "user@test.local";
    static final String CODE = "123456";

    AtomicReference<EmailAuth> stored = new AtomicReference<>();
    EmailAuthRepository repo;
    AuthService authService;
    EmailAuthService emailAuthService;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        repo = mock(EmailAuthRepository.class);
        // 저장소 흉내: 가장 최근 인증번호 하나만 기억하고, delete 하면 사라진다.
        when(repo.save(any())).thenAnswer(inv -> { stored.set(inv.getArgument(0)); return inv.getArgument(0); });
        when(repo.findTopByEmailOrderByExpiryDateDesc(EMAIL)).thenAnswer(inv -> Optional.ofNullable(stored.get()));
        doAnswer(inv -> { stored.set(null); return null; }).when(repo).delete(any());
        stored.set(EmailAuth.builder().email(EMAIL).authCode(CODE).expiryDate(LocalDateTime.now().plusMinutes(3)).build());

        authService = mock(AuthService.class);
        when(authService.checkEmail(EMAIL)).thenReturn(true);
        emailAuthService = new EmailAuthService(mock(JavaMailSender.class), repo);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, emailAuthService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private String resetBody(String code) {
        return "{\"email\":\"" + EMAIL + "\",\"code\":\"" + code + "\",\"newPassword\":\"Newpass1!x\"}";
    }

    @Test
    @DisplayName("인증번호를 5번 틀리면 그 번호는 더 쓸 수 없다 (6자리 무차별 대입 막기)")
    void bruteForceStopsAfterFiveWrongCodes() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(patch("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                    .content(resetBody(String.format("%06d", i)))).andExpect(status().isBadRequest());
        }
        // 맞는 번호를 넣어도 이제는 실패해야 한다 — 새 번호를 받아야 함
        mvc.perform(patch("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody(CODE))).andExpect(status().isBadRequest());
        verify(authService, never()).updatePassword(anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호를 바꾼 인증번호로 3분 안에 한 번 더 바꿀 수 없다")
    void codeCannotBeReusedAfterReset() throws Exception {
        mvc.perform(patch("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody(CODE))).andExpect(status().isOk());
        mvc.perform(patch("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody(CODE))).andExpect(status().isBadRequest());
        verify(authService, times(1)).updatePassword(eq(EMAIL), anyString());
    }

    @Test
    @DisplayName("정상: 화면 순서대로 번호 확인 → 비밀번호 변경이 된다 (확인 단계에서 번호를 지우지 않음)")
    void normalResetFlowStillWorks() throws Exception {
        mvc.perform(post("/api/auth/verify-email").param("email", EMAIL).param("code", CODE))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody(CODE))).andExpect(status().isOk());
        verify(authService).updatePassword(eq(EMAIL), anyString());
    }

    @Test
    @DisplayName("정상: 한두 번 틀린 뒤 맞는 번호를 넣으면 통과한다")
    void typoThenCorrect() throws Exception {
        mvc.perform(post("/api/auth/verify-email").param("email", EMAIL).param("code", "000000"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/verify-email").param("email", EMAIL).param("code", CODE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("같은 이메일로 인증 메일을 10분에 5번 넘게 보낼 수 없다 (메일 폭탄 · 발송 한도 소진 막기)")
    void sendIsRateLimited() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/send-email").param("email", EMAIL)).andExpect(status().isOk());
        }
        mvc.perform(post("/api/auth/send-email").param("email", EMAIL)).andExpect(status().isTooManyRequests());
        // 다른 주소는 영향 없음
        mvc.perform(post("/api/auth/send-email").param("email", "someone@test.local")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 인증을 하지 않고 주소만 보내 가입할 수 없다 (서버에서도 확인)")
    void signupNeedsServerSideVerification() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newbie1\",\"password\":\"Newpass1!x\",\"name\":\"새회원\",\"email\":\"" + EMAIL + "\",\"region\":\"서울\"}"))
                .andExpect(status().isBadRequest());
        verify(authService, never()).signUp(any(SignUpRequestDTO.class));
    }

    @Test
    @DisplayName("정상: 이메일 인증을 마친 뒤에는 가입된다")
    void signupAfterVerification() throws Exception {
        mvc.perform(post("/api/auth/verify-email").param("email", EMAIL).param("code", CODE))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newbie1\",\"password\":\"Newpass1!x\",\"name\":\"새회원\",\"email\":\"" + EMAIL + "\",\"region\":\"서울\"}"))
                .andExpect(status().isOk());
        verify(authService).signUp(any(SignUpRequestDTO.class));
    }
}
