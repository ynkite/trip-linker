package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.config.SecurityConfig;
import idusw.sbb.triplinker.domain.auth.filter.JwtAuthenticationFilter;
import idusw.sbb.triplinker.domain.auth.security.OAuth2FailureHandler;
import idusw.sbb.triplinker.domain.auth.security.OAuth2SuccessHandler;
import idusw.sbb.triplinker.domain.auth.service.CustomOAuth2UserService;
import idusw.sbb.triplinker.domain.auth.service.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * 실제 SecurityConfig 를 띄워 「누가 어느 주소를 열 수 있는가」 와 응답 보안 헤더를 본다.
 * 컨트롤러는 같은 주소를 가진 가짜(Stub)로 바꿔 끼워 DB 없이 돈다.
 */
@WebMvcTest(controllers = SecurityConfigWebTest.Stub.class, properties = {
        "file.upload.path=build/tmp/uploads/", "file.upload.url=/uploads/posts/"})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityConfigWebTest.OAuthConfig.class, SecurityConfigWebTest.Stub.class})
class SecurityConfigWebTest {

    @RestController
    static class Stub {
        @GetMapping("/") String home() { return "ok"; }
        @GetMapping("/api/trips/{id}") String trip() { return "ok"; }
        @GetMapping("/api/trips/{id}/routes") String routes() { return "ok"; }
        @GetMapping("/api/trips/{id}/input-form") String form() { return "ok"; }
        @PostMapping("/api/trips/{id}/expenses") String addExpense() { return "ok"; }
        @PostMapping("/api/auth/refresh") String refresh() { return "ok"; }
        @GetMapping("/api/admin/users") String admin() { return "ok"; }
        @GetMapping("/api/demo-mode") String demo() { return "ok"; }
        @GetMapping("/robots.txt") String robots() { return "ok"; }
    }

    @TestConfiguration
    static class OAuthConfig {
        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(ClientRegistration.withRegistrationId("google")
                    .clientId("test").clientSecret("test").authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .authorizationUri("https://example.invalid/auth").tokenUri("https://example.invalid/token").build());
        }
    }

    @Autowired MockMvc mvc;
    @MockitoBean JwtProvider jwtProvider;
    @MockitoBean org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext; // @EnableJpaAuditing 용
    @MockitoBean CustomOAuth2UserService customOAuth2UserService;
    @MockitoBean OAuth2SuccessHandler oAuth2SuccessHandler;
    @MockitoBean OAuth2FailureHandler oAuth2FailureHandler;

    private int status(MvcResult r) { return r.getResponse().getStatus(); }

    @Test
    @DisplayName("정상: 공유 링크 읽기 주소는 로그인 없이 열린다")
    void sharedReadIsPublic() throws Exception {
        for (String p : new String[]{"/", "/api/trips/10", "/api/trips/10/routes", "/api/trips/10/input-form", "/api/demo-mode"}) {
            assertThat(status(mvc.perform(get(p)).andReturn())).as(p).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("로그인 없이 지출을 넣을 수 없고, 일반 회원은 관리자 주소를 못 연다")
    void protectedStaysProtected() throws Exception {
        assertThat(status(mvc.perform(post("/api/trips/10/expenses")).andReturn())).isIn(302, 401, 403); // 302 = 소셜 로그인 설정이 로그인 화면(/)으로 돌려보냄(기존 동작)
        assertThat(status(mvc.perform(get("/api/admin/users")).andReturn())).isIn(302, 401, 403); // 302 = 소셜 로그인 설정이 로그인 화면(/)으로 돌려보냄(기존 동작)
        when(jwtProvider.validateToken(anyString())).thenReturn(true);
        when(jwtProvider.getUserIdFromToken(anyString())).thenReturn(2L);
        when(jwtProvider.getRoleFromToken(anyString())).thenReturn("USER");
        assertThat(status(mvc.perform(get("/api/admin/users").header("Authorization", "Bearer t")).andReturn())).isEqualTo(403);
    }

    @Test
    @DisplayName("접속 토큰이 만료된 뒤에도 재발급 주소는 열린다 (재발급 토큰 자체로 확인)")
    void refreshWorksWithoutAccessToken() throws Exception {
        assertThat(status(mvc.perform(post("/api/auth/refresh").contentType("application/json").content("{}")).andReturn()))
                .isEqualTo(200);
    }

    @Test
    @DisplayName("응답에 Referrer-Policy · Permissions-Policy · CSP 가 붙는다")
    void securityHeaders() throws Exception {
        mvc.perform(get("/"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().exists("Permissions-Policy"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Content-Security-Policy-Report-Only"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("다른 사이트에서 온 요청에 인증 정보 포함(credentials) 허용을 내주지 않는다")
    void corsDoesNotAllowCredentialsForAnyOrigin() throws Exception {
        MvcResult r = mvc.perform(options("/api/trips/10").header("Origin", "https://evil.example")
                .header("Access-Control-Request-Method", "GET")).andReturn();
        assertThat(r.getResponse().getHeader("Access-Control-Allow-Credentials")).isNotEqualTo("true");
    }

    @Test
    @DisplayName("robots.txt 는 로그인 없이 열린다 (검색 로봇이 HTML 대신 받던 문제)")
    void robotsPublic() throws Exception {
        assertThat(status(mvc.perform(get("/robots.txt")).andReturn())).isEqualTo(200);
    }
}
