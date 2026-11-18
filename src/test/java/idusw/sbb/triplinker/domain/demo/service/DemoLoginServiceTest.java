package idusw.sbb.triplinker.domain.demo.service;

import idusw.sbb.triplinker.domain.auth.dto.TokenResponseDto;
import idusw.sbb.triplinker.domain.auth.entity.RefreshToken;
import idusw.sbb.triplinker.domain.auth.repository.RefreshTokenRepository;
import idusw.sbb.triplinker.domain.auth.service.JwtProvider;
import idusw.sbb.triplinker.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DemoLoginServiceTest {

    // 실제 JwtProvider 로 만든 토큰을 실제 검증 로직으로 확인한다
    private final JwtProvider jwt = new JwtProvider(
            "test-secret-key-test-secret-key-test-secret-key-0123456789", 60_000L, 600_000L);

    private DemoAccountService accounts;
    private DemoSeedService seed;
    private RefreshTokenRepository refreshRepo;
    private DemoLoginService service;
    private User demoUser;
    private User demoAdmin;

    @BeforeEach
    void setUp() {
        accounts = mock(DemoAccountService.class);
        seed = mock(DemoSeedService.class);
        refreshRepo = mock(RefreshTokenRepository.class);
        service = new DemoLoginService(accounts, seed, jwt, refreshRepo);

        demoUser = User.builder().username("demo_user").name("테스트 유저").email("demo_user@example.com")
                .region("서울").role("USER").build();
        ReflectionTestUtils.setField(demoUser, "id", 900L);
        demoAdmin = User.builder().username("demo_admin").name("테스트 관리자").email("demo_admin@example.com")
                .region("서울").role("DEMO_ADMIN").build();
        ReflectionTestUtils.setField(demoAdmin, "id", 901L);
        when(accounts.getOrCreateUser()).thenReturn(demoUser);
        when(accounts.getOrCreateAdmin()).thenReturn(demoAdmin);
    }

    @Test
    @DisplayName("유저 단추: 테스트 유저 토큰을 주고, 데이터를 되돌리고, refresh 토큰은 해시로만 저장한다")
    void userLogin() {
        TokenResponseDto t = service.login(DemoLoginService.Kind.USER);

        assertThat(jwt.validateToken(t.accessToken())).isTrue();
        assertThat(jwt.getUserIdFromToken(t.accessToken())).isEqualTo(900L);
        assertThat(jwt.getRoleFromToken(t.accessToken())).isEqualTo("USER");
        assertThat(t.isPasswordChangeRecommended()).isFalse();

        verify(seed).restoreAccount(900L, "USER", DemoAccountService.USER_NAME);
        verify(seed).reset(eq(900L), any());

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshRepo).save(saved.capture());
        assertThat(saved.getValue().getTokenHash()).hasSize(64).isNotEqualTo(t.refreshToken());
        // 같은 계정을 여러 사람이 쓰므로 다른 사람의 refresh 토큰을 지우지 않는다
        verify(refreshRepo, never()).deleteByUserId(anyLong());
    }

    @Test
    @DisplayName("관리자 단추: DEMO_ADMIN 권한 토큰. 진짜 ADMIN 권한은 나가지 않고 데이터 되돌리기도 없다")
    void adminLogin() {
        TokenResponseDto t = service.login(DemoLoginService.Kind.ADMIN);

        assertThat(jwt.getUserIdFromToken(t.accessToken())).isEqualTo(901L);
        assertThat(jwt.getRoleFromToken(t.accessToken())).isEqualTo("DEMO_ADMIN");
        verify(seed).restoreAccount(901L, "DEMO_ADMIN", DemoAccountService.ADMIN_NAME);
        verify(seed, never()).reset(anyLong(), any());
    }

    @Test
    @DisplayName("되돌리기가 실패해도 로그인은 된다")
    void resetFailureStillLogsIn() {
        doThrow(new RuntimeException("DB 오류")).when(seed).reset(anyLong(), any());

        TokenResponseDto t = service.login(DemoLoginService.Kind.USER);

        assertThat(jwt.validateToken(t.accessToken())).isTrue();
    }

    @Test
    @DisplayName("여럿이 동시에 눌러도 되돌리기는 한 번에 하나만 돌고, 모두 로그인된다")
    void concurrentResetsRunOneAtATime() throws Exception {
        AtomicInteger running = new AtomicInteger();
        AtomicInteger maxRunning = new AtomicInteger();
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(inv -> {
            maxRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
            release.await(2, TimeUnit.SECONDS);
            running.decrementAndGet();
            return null;
        }).when(seed).reset(anyLong(), any());

        ExecutorService pool = Executors.newFixedThreadPool(5);
        try {
            Future<TokenResponseDto> first = pool.submit(() -> service.login(DemoLoginService.Kind.USER));
            // 첫 번째가 되돌리기에 들어갈 때까지 기다린다
            for (int i = 0; i < 100 && running.get() == 0; i++) Thread.sleep(10);

            Future<?>[] others = new Future<?>[4];
            for (int i = 0; i < 4; i++) {
                others[i] = pool.submit(() -> service.login(DemoLoginService.Kind.USER));
            }
            for (Future<?> f : others) {
                assertThat(f.get(2, TimeUnit.SECONDS)).isNotNull(); // 기다리지 않고 바로 로그인
            }
            release.countDown();
            assertThat(first.get(3, TimeUnit.SECONDS)).isNotNull();
        } finally {
            pool.shutdownNow();
        }
        assertThat(maxRunning.get()).isEqualTo(1);
        verify(seed, times(1)).reset(anyLong(), any());
    }
}
