package idusw.sbb.triplinker.global.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * 로그인한 상태에서 비밀번호를 확인하는 주소의 횟수 제한 (KISA 2-20 자동화 공격).
 * 로그인은 5번 틀리면 5분 잠기지만(User.increaseLoginFail), 현재 비밀번호 확인 · 비밀번호 변경 · 탈퇴는
 * 제한이 없어서 토큰만 있으면 비밀번호를 계속 넣어 볼 수 있었다.
 * 세 주소를 합쳐 회원별 10분에 10번. 계정을 잠그지 않고 그 회원의 이 요청만 잠시 막는다
 * (토큰을 가진 남이 일부러 틀려 본인 로그인까지 잠기게 하지 않으려고).
 * 서버 한 대라 메모리에 둔다(AiCallLimitInterceptor 와 같은 방식).
 */
@Component
public class PasswordCheckLimitInterceptor implements HandlerInterceptor {

    static final int LIMIT = 10;
    static final long WINDOW_MS = 10 * 60 * 1000L;

    private final Map<String, Deque<Long>> calls = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LongSupplier clock;

    public PasswordCheckLimitInterceptor() {
        this(System::currentTimeMillis);
    }

    PasswordCheckLimitInterceptor(LongSupplier clock) {
        this.clock = clock;
    }

    static boolean isPasswordCheck(String method, String uri) {
        return ("POST".equalsIgnoreCase(method) && uri.endsWith("/api/users/me/verify-password"))
                || ("PATCH".equalsIgnoreCase(method) && uri.endsWith("/api/users/me/password"))
                || ("DELETE".equalsIgnoreCase(method) && uri.matches(".*/api/users/me/?$"));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!isPasswordCheck(request.getMethod(), request.getRequestURI())) return true;
        if (allow(caller(request))) return true;

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(WINDOW_MS / 1000));
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.error("비밀번호 확인 요청이 너무 많습니다. 10분 뒤에 다시 시도해 주세요.")));
        return false;
    }

    boolean allow(String who) {
        long now = clock.getAsLong();
        Deque<Long> log = calls.computeIfAbsent(who, k -> new ArrayDeque<>());
        synchronized (log) {
            while (!log.isEmpty() && now - log.peekFirst() > WINDOW_MS) log.pollFirst();
            if (log.size() >= LIMIT) return false;
            log.addLast(now);
            return true;
        }
    }

    private static String caller(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails u && u.getUserId() != null) {
            return "u" + u.getUserId();
        }
        return "ip" + request.getRemoteAddr();
    }
}
