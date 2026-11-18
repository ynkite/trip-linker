package idusw.sbb.triplinker.domain.demo;

import idusw.sbb.triplinker.domain.auth.dto.LoginRequestDto;
import idusw.sbb.triplinker.domain.auth.dto.TokenResponseDto;
import idusw.sbb.triplinker.domain.auth.repository.OAuthAccountRepository;
import idusw.sbb.triplinker.domain.auth.repository.PasswordResetTokenRepository;
import idusw.sbb.triplinker.domain.auth.repository.RefreshTokenRepository;
import idusw.sbb.triplinker.domain.auth.service.AuthServiceImpl;
import idusw.sbb.triplinker.domain.auth.service.JwtProvider;
import idusw.sbb.triplinker.domain.demo.service.DemoAccountService;
import idusw.sbb.triplinker.domain.user.dto.UserInfoResponseDto;
import idusw.sbb.triplinker.domain.user.entity.User;
import idusw.sbb.triplinker.domain.user.repository.UserRepository;
import idusw.sbb.triplinker.domain.user.repository.UserSecurityHistoryRepository;
import idusw.sbb.triplinker.global.exception.LoginFailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 테스트 로그인을 더한 뒤에도 기존 아이디/비밀번호 로그인 흐름이 그대로인지. */
class ExistingLoginFlowTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtProvider jwt = new JwtProvider(
            "test-secret-key-test-secret-key-test-secret-key-0123456789", 60_000L, 600_000L);

    private UserRepository users;
    private RefreshTokenRepository refresh;
    private AuthServiceImpl auth;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        refresh = mock(RefreshTokenRepository.class);
        auth = new AuthServiceImpl(users, mock(UserSecurityHistoryRepository.class), refresh,
                mock(PasswordResetTokenRepository.class), encoder, jwt, mock(OAuthAccountRepository.class));
    }

    private User user(long id, String username, String rawPw, String role) {
        User u = User.builder().username(username).passwordHash(encoder.encode(rawPw)).name(username)
                .email(username + "@test.com").region("서울").role(role).build();
        ReflectionTestUtils.setField(u, "id", id);
        when(users.findByUsername(username)).thenReturn(Optional.of(u));
        return u;
    }

    @Test
    @DisplayName("일반 로그인: 맞는 비밀번호면 그 계정 권한으로 토큰이 나오고 기존 refresh 토큰을 지운다(종전과 같음)")
    void normalLoginUnchanged() {
        user(1L, "user01", "1234", "USER");
        user(2L, "admin", "1234", "ADMIN");

        TokenResponseDto u = auth.login(new LoginRequestDto("user01", "1234"), "127.0.0.1");
        TokenResponseDto a = auth.login(new LoginRequestDto("admin", "1234"), "127.0.0.1");

        assertThat(jwt.getRoleFromToken(u.accessToken())).isEqualTo("USER");
        assertThat(jwt.getRoleFromToken(a.accessToken())).isEqualTo("ADMIN");
        verify(refresh).deleteByUserId(1L);
        verify(refresh).deleteByUserId(2L);
    }

    @Test
    @DisplayName("테스트 계정은 아이디/비밀번호로는 들어갈 수 없다(비밀번호가 아무도 모르는 무작위 값)")
    void demoAccountCannotUsePasswordLogin() {
        when(users.findByUsername("demo_user")).thenReturn(Optional.empty());
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 900L);
            return u;
        });
        new DemoAccountService(users).getOrCreateUser();
        ArgumentCaptor<User> created = ArgumentCaptor.forClass(User.class);
        verify(users).save(created.capture());
        User demo = created.getValue();
        when(users.findByUsername("demo_user")).thenReturn(Optional.of(demo));

        for (String guess : new String[]{"", "1234", "demo_user", "demo", "password"}) {
            assertThatThrownBy(() -> auth.login(new LoginRequestDto("demo_user", guess), "127.0.0.1"))
                    .isInstanceOf(LoginFailException.class);
        }
        assertThat(demo.getRole()).isEqualTo("USER");
        assertThat(demo.getUsername()).isEqualTo("demo_user");
    }

    @Test
    @DisplayName("내 정보 응답의 권한: USER·ADMIN 은 그대로, 테스트 관리자만 화면용으로 ADMIN")
    void meRole() {
        User normal = User.builder().username("u").name("n").email("e").region("r").role("USER").build();
        User admin = User.builder().username("a").name("n").email("e2").region("r").role("ADMIN").build();
        User demo = User.builder().username("d").name("n").email("e3").region("r").role("DEMO_ADMIN").build();
        assertThat(new UserInfoResponseDto(normal, false).getRole()).isEqualTo("USER");
        assertThat(new UserInfoResponseDto(admin, false).getRole()).isEqualTo("ADMIN");
        assertThat(new UserInfoResponseDto(demo, false).getRole()).isEqualTo("ADMIN");
    }
}
