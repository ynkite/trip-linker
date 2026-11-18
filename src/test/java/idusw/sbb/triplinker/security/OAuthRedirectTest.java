package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.domain.auth.repository.RefreshTokenRepository;
import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.domain.auth.security.OAuth2SuccessHandler;
import idusw.sbb.triplinker.domain.auth.service.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** OWASP A07 — 소셜 로그인 뒤 토큰이 주소 쿼리(?accessToken=)로 나가 접속 기록 · Referer 에 남던 문제. */
class OAuthRedirectTest {

    @Test
    @DisplayName("소셜 로그인 뒤 토큰은 # 뒤로만 넘기고 ? 쿼리에는 싣지 않는다")
    void tokensGoInFragmentOnly() throws Exception {
        JwtProvider jwt = mock(JwtProvider.class);
        when(jwt.createAccessToken(7L, "USER")).thenReturn("aaa.bbb.ccc");
        when(jwt.createRefreshToken(7L)).thenReturn("rrr.sss.ttt");
        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(jwt, mock(RefreshTokenRepository.class));

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/login/oauth2/code/kakao");
        req.setScheme("https");
        req.setServerName("triplinker.r-e.kr");
        req.setServerPort(443);
        MockHttpServletResponse res = new MockHttpServletResponse();
        CustomUserDetails principal = new CustomUserDetails(7L, "USER");

        handler.onAuthenticationSuccess(req, res, new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        String url = res.getRedirectedUrl();
        assertThat(url).isEqualTo("https://triplinker.r-e.kr/#accessToken=aaa.bbb.ccc&refreshToken=rrr.sss.ttt");
        assertThat(url).doesNotContain("?");
    }
}
