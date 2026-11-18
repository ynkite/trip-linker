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
 * AI(Claude) 를 부르는 주소에 회원별 호출 횟수 제한을 건다 (OWASP LLM「무제한 소비」).
 * 전에는 로그인한 한 사람이 반복 호출로 요금을 끝없이 쓸 수 있었다.
 * - 챗봇 메시지: 10분에 40번
 * - 일정 생성 · 장소 교체 · 실내 교체: 1시간에 30번 (생성 1번 + 자동 보정 최대 2번 + 손으로 교체 여러 번을 넉넉히 덮음)
 * 서버 한 대라 메모리에 둔다(재시작하면 비워짐). 넘으면 429 와 안내 문구.
 */
@Component
public class AiCallLimitInterceptor implements HandlerInterceptor {

    static final int CHAT_LIMIT = 40;
    static final long CHAT_WINDOW_MS = 10 * 60 * 1000L;
    static final int ROUTE_LIMIT = 30;
    static final long ROUTE_WINDOW_MS = 60 * 60 * 1000L;

    private final Map<String, Deque<Long>> calls = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LongSupplier clock;

    public AiCallLimitInterceptor() {
        this(System::currentTimeMillis);
    }

    AiCallLimitInterceptor(LongSupplier clock) {
        this.clock = clock;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        String uri = request.getRequestURI();
        boolean chat = uri.endsWith("/api/chat/message");
        boolean route = uri.matches(".*/api/trips/[^/]+/routes/(generate|replace|indoor-replace)$");
        if (!chat && !route) return true;

        String who = caller(request);
        String bucket = (chat ? "chat:" : "route:") + who;
        int limit = chat ? CHAT_LIMIT : ROUTE_LIMIT;
        long window = chat ? CHAT_WINDOW_MS : ROUTE_WINDOW_MS;

        if (allow(bucket, limit, window)) return true;

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(window / 1000));
        String msg = chat
                ? "AI 상담 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
                : "AI 일정 생성 · 교체 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.";
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(msg)));
        return false;
    }

    boolean allow(String bucket, int limit, long windowMs) {
        long now = clock.getAsLong();
        Deque<Long> log = calls.computeIfAbsent(bucket, k -> new ArrayDeque<>());
        synchronized (log) {
            while (!log.isEmpty() && now - log.peekFirst() > windowMs) log.pollFirst();
            if (log.size() >= limit) return false;
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
