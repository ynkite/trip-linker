package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.domain.auth.controller.AuthController;
import idusw.sbb.triplinker.domain.auth.dto.LoginRequestDto;
import idusw.sbb.triplinker.domain.auth.dto.TokenResponseDto;
import idusw.sbb.triplinker.domain.auth.repository.OAuthAccountRepository;
import idusw.sbb.triplinker.domain.auth.repository.PasswordResetTokenRepository;
import idusw.sbb.triplinker.domain.auth.repository.RefreshTokenRepository;
import idusw.sbb.triplinker.domain.auth.service.AuthServiceImpl;
import idusw.sbb.triplinker.domain.auth.service.EmailAuthService;
import idusw.sbb.triplinker.domain.auth.service.JwtProvider;
import idusw.sbb.triplinker.domain.user.entity.User;
import idusw.sbb.triplinker.domain.user.repository.UserRepository;
import idusw.sbb.triplinker.domain.user.repository.UserSecurityHistoryRepository;
import idusw.sbb.triplinker.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * KISA BF(약한 비밀번호) 남은 1건: 규칙이 생기기 전에 만든 관리자 · 테스트용 계정의 쉬운 비밀번호.
 * 비밀번호가 맞아도 지금 규칙(8~16자 영문 · 숫자 · 특수문자)에 맞지 않으면 토큰을 내주지 않고 재설정으로 보낸다.
 */
class WeakPasswordLoginTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtProvider jwt = new JwtProvider(
            "test-secret-key-test-secret-key-test-secret-key-0123456789", 60_000L, 600_000L);

    private UserRepository users;
    private RefreshTokenRepository refresh;
    private AuthServiceImpl auth;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        refresh = mock(RefreshTokenRepository.class);
        auth = new AuthServiceImpl(users, mock(UserSecurityHistoryRepository.class), refresh,
                mock(PasswordResetTokenRepository.class), encoder, jwt, mock(OAuthAccountRepository.class));
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(auth, mock(EmailAuthService.class)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private User user(long id, String username, String rawPw, String role) {
        User u = User.builder().username(username).passwordHash(encoder.encode(rawPw)).name(username)
                .email(username + "@test.com").region("서울").role(role).build();
        ReflectionTestUtils.setField(u, "id", id);
        when(users.findByUsername(username)).thenReturn(Optional.of(u));
        return u;
    }

    private String body(String id, String pw) {
        return "{\"username\":\"" + id + "\",\"password\":\"" + pw + "\"}";
    }

    @Test
    @DisplayName("관리자가 규칙에 맞지 않는 옛 비밀번호(1234)로 맞게 넣어도 토큰이 나오지 않는다")
    void weakAdminPasswordGetsNoToken() throws Exception {
        user(2L, "admin", "1234", "ADMIN");

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body("admin", "1234")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.weakPassword").value(true))
                .andExpect(jsonPath("$.data.accessToken").doesNotExist());

        verify(refresh, never()).save(any());
    }

    @Test
    @DisplayName("일반 회원도 같다: 옛 규칙으로 만든 약한 비밀번호(영문만 8자)로는 토큰이 나오지 않는다")
    void weakUserPasswordGetsNoToken() throws Exception {
        user(1L, "user01", "abcdefgh", "USER");

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body("user01", "abcdefgh")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.data.weakPassword").value(true));
        verify(refresh, never()).save(any());
    }

    @Test
    @DisplayName("약한 비밀번호라도 틀리게 넣으면 종전처럼 실패 횟수가 오른다 (규칙 안내가 비밀번호 맞힘 여부를 먼저 알려 주지 않음)")
    void wrongPasswordStillCountsAsFailure() throws Exception {
        User u = user(1L, "user01", "1234", "USER");

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body("user01", "12345")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.failCount").value(1))
                .andExpect(jsonPath("$.data.weakPassword").doesNotExist());
        assertThat(u.getLoginFailCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("규칙에 맞는 비밀번호는 종전처럼 로그인된다")
    void strongPasswordLogsIn() {
        user(2L, "admin", "Adm1n!pass", "ADMIN");

        TokenResponseDto t = auth.login(new LoginRequestDto("admin", "Adm1n!pass"), "127.0.0.1");

        assertThat(jwt.getRoleFromToken(t.accessToken())).isEqualTo("ADMIN");
        verify(refresh).save(any());
    }
}
