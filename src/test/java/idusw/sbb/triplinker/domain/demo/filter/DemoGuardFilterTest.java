package idusw.sbb.triplinker.domain.demo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import idusw.sbb.triplinker.domain.demo.service.DemoAccountService;
import idusw.sbb.triplinker.domain.demo.service.DemoUsageLimiter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class DemoGuardFilterTest {

    private static final long DEMO_USER_ID = 900L;
    private static final long DEMO_ADMIN_ID = 901L;
    private static final long REAL_USER_ID = 1L;

    private DemoAccountService accounts;
    private DemoGuardFilter filter;

    @BeforeEach
    void setUp() {
        accounts = mock(DemoAccountService.class);
        when(accounts.isDemoAccount(DEMO_USER_ID)).thenReturn(true);
        when(accounts.isDemoAccount(DEMO_ADMIN_ID)).thenReturn(true);
        when(accounts.isDemoAccount(REAL_USER_ID)).thenReturn(false);
        DemoLoginProperties props = new DemoLoginProperties(true, 2, 10);
        filter = new DemoGuardFilter(accounts, new DemoUsageLimiter(props), props, new ObjectMapper());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(long userId, String role) {
        CustomUserDetails principal = new CustomUserDetails(userId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private MockHttpServletResponse run(String method, String path, MockFilterChain chain) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest(method, path);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        return res;
    }

    @Test
    @DisplayName("테스트 관리자가 정지·권한 변경·삭제·설정 변경을 하면 403 과 안내 문구")
    void demoAdminWritesAreForbidden() throws Exception {
        loginAs(DEMO_ADMIN_ID, "DEMO_ADMIN");
        String[][] calls = {
                {"PATCH", "/api/admin/users/3/suspend"},
                {"PATCH", "/api/admin/users/3/promote"},
                {"PATCH", "/api/admin/users/3/demote"},
                {"DELETE", "/api/admin/curations/1"},
                {"POST", "/api/admin/curations"},
                {"PATCH", "/api/admin/reports/5/hide"},
                {"DELETE", "/api/posts/7"},
        };
        for (String[] c : calls) {
            MockFilterChain chain = new MockFilterChain();
            MockHttpServletResponse res = run(c[0], c[1], chain);
            assertThat(res.getStatus()).as(c[0] + " " + c[1]).isEqualTo(403);
            assertThat(res.getContentAsString(java.nio.charset.StandardCharsets.UTF_8))
                    .contains("테스트 계정은 바꿀 수 없습니다");
            assertThat(chain.getRequest()).as("컨트롤러까지 가면 안 된다").isNull();
        }
    }

    @Test
    @DisplayName("테스트 관리자도 관리자 화면 읽기와 로그아웃은 된다")
    void demoAdminCanRead() throws Exception {
        loginAs(DEMO_ADMIN_ID, "DEMO_ADMIN");
        for (String[] c : new String[][]{
                {"GET", "/api/admin/dashboard"}, {"GET", "/api/admin/users"},
                {"GET", "/api/admin/reports"}, {"POST", "/api/auth/logout"}}) {
            MockFilterChain chain = new MockFilterChain();
            run(c[0], c[1], chain);
            assertThat(chain.getRequest()).as(c[0] + " " + c[1]).isNotNull();
        }
    }

    @Test
    @DisplayName("진짜 관리자와 일반 회원 요청은 그대로 지나간다")
    void realAccountsPassThrough() throws Exception {
        loginAs(2L, "ADMIN");
        MockFilterChain chain = new MockFilterChain();
        run("PATCH", "/api/admin/users/3/suspend", chain);
        assertThat(chain.getRequest()).isNotNull();

        loginAs(REAL_USER_ID, "USER");
        for (int i = 0; i < 5; i++) {
            chain = new MockFilterChain();
            run("POST", "/api/chat/message", chain);
            assertThat(chain.getRequest()).isNotNull();
        }
        chain = new MockFilterChain();
        run("DELETE", "/api/users/me", chain);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("일반 회원의 보통 요청에는 DB 조회를 하지 않는다")
    void noLookupForOrdinaryRequests() throws Exception {
        loginAs(REAL_USER_ID, "USER");
        run("POST", "/api/trips", new MockFilterChain());
        run("GET", "/api/trips/1", new MockFilterChain());
        verify(accounts, never()).isDemoAccount(anyLong());
    }

    @Test
    @DisplayName("테스트 유저는 탈퇴·비밀번호 변경이 막힌다")
    void demoUserCannotWithdraw() throws Exception {
        loginAs(DEMO_USER_ID, "USER");
        assertThat(run("DELETE", "/api/users/me", new MockFilterChain()).getStatus()).isEqualTo(403);
        assertThat(run("PATCH", "/api/users/me/password", new MockFilterChain()).getStatus()).isEqualTo(403);
        // 프로필 수정은 다음 로그인 때 되돌아가므로 허용
        MockFilterChain chain = new MockFilterChain();
        run("PATCH", "/api/users/me", chain);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("테스트 유저의 AI 호출은 하루 한도(여기선 2번)를 넘으면 429 와 안내 문구")
    void demoUserAiLimit() throws Exception {
        loginAs(DEMO_USER_ID, "USER");
        assertThat(run("POST", "/api/trips/11/routes/generate", new MockFilterChain()).getStatus()).isEqualTo(200);
        assertThat(run("POST", "/api/chat/message", new MockFilterChain()).getStatus()).isEqualTo(200);
        MockHttpServletResponse third = run("POST", "/api/trips/11/routes/replace", new MockFilterChain());
        assertThat(third.getStatus()).isEqualTo(429);
        assertThat(third.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("하루 2번");
        // AI 가 아닌 요청은 계속 된다
        MockFilterChain chain = new MockFilterChain();
        run("POST", "/api/trips/11/expenses", chain);
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    @DisplayName("로그인 안 한 요청은 건드리지 않는다")
    void anonymousPassThrough() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        run("POST", "/api/auth/login", chain);
        assertThat(chain.getRequest()).isNotNull();
    }
}
