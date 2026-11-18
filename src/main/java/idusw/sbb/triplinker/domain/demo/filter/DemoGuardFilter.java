package idusw.sbb.triplinker.domain.demo.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import idusw.sbb.triplinker.domain.demo.service.DemoAccountService;
import idusw.sbb.triplinker.domain.demo.service.DemoUsageLimiter;
import idusw.sbb.triplinker.global.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 테스트 계정 전용 제한. SecurityConfig 에서 JWT 필터 바로 뒤에 붙는다.
 *
 * 1) 테스트 관리자(ROLE_DEMO_ADMIN)는 읽기만. GET 이 아닌 요청은 403 「테스트 계정은 바꿀 수 없습니다」.
 * 2) 테스트 계정은 탈퇴·비밀번호 변경을 할 수 없다(403).
 * 3) 테스트 계정의 AI 호출(일정 생성·교체, 챗봇)은 하루 횟수 제한(429).
 *
 * 일반 회원 요청은 그대로 통과한다. 이 필터가 DB를 보는 것은 AI·탈퇴·비밀번호 주소일 때뿐이다.
 */
@Component
@RequiredArgsConstructor
public class DemoGuardFilter extends OncePerRequestFilter {

    /** 테스트 관리자가 GET 말고도 부를 수 있는 주소(자기 세션 정리용). */
    private static final Set<String> ADMIN_WRITE_ALLOWED = Set.of(
            "/api/auth/logout",
            "/api/auth/refresh",
            "/api/auth/demo",
            "/api/auth/pw-noti-later");

    private static final Pattern AI_PATH = Pattern.compile(
            "^/api/trips/[^/]+/routes/(generate|replace|indoor-replace)/?$|^/api/chat/message/?$");

    private static final Pattern WITHDRAW_PATH = Pattern.compile("^/api/users/me/?$");
    private static final Pattern PASSWORD_PATH = Pattern.compile("^/api/users/me/password/?$");

    private final DemoAccountService accountService;
    private final DemoUsageLimiter limiter;
    private final DemoLoginProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails principal)) {
            chain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean write = !("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method));
        boolean demoAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + DemoLoginProperties.ROLE_DEMO_ADMIN).equals(a.getAuthority()));

        if (demoAdmin && write && !ADMIN_WRITE_ALLOWED.contains(path)) {
            reject(response, HttpStatus.FORBIDDEN, DemoLoginProperties.READ_ONLY_MESSAGE);
            return;
        }

        // 탈퇴(DELETE /api/users/me), 비밀번호 변경(PATCH /api/users/me/password)
        boolean accountChange = ("DELETE".equals(method) && WITHDRAW_PATH.matcher(path).matches())
                || ("PATCH".equals(method) && PASSWORD_PATH.matcher(path).matches());
        boolean ai = "POST".equals(method) && AI_PATH.matcher(path).matches();

        if ((accountChange || ai) && (demoAdmin || accountService.isDemoAccount(principal.getUserId()))) {
            if (accountChange) {
                reject(response, HttpStatus.FORBIDDEN, DemoLoginProperties.READ_ONLY_MESSAGE);
                return;
            }
            if (!limiter.tryAi(principal.getUserId())) {
                reject(response, HttpStatus.TOO_MANY_REQUESTS,
                        "테스트 계정은 AI 기능을 하루 " + properties.getAiDailyLimit()
                                + "번까지 쓸 수 있습니다. 내일 다시 써 주세요.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
