package idusw.sbb.triplinker.domain.demo.service;

import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 테스트 로그인 호출 횟수(IP당 1분)와 테스트 계정의 AI 호출 횟수(계정당 하루)를 센다.
 *
 * 서버 메모리에만 둔다. 서버가 다시 뜨면 0부터 센다. 서버가 한 대라는 전제다.
 */
@Component
public class DemoUsageLimiter {

    private final DemoLoginProperties properties;
    private final Clock clock;

    @Autowired
    public DemoUsageLimiter(DemoLoginProperties properties) {
        this(properties, Clock.systemDefaultZone());
    }

    DemoUsageLimiter(DemoLoginProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    private final Map<String, Window> loginWindows = new ConcurrentHashMap<>();
    private final Map<Long, Window> aiWindows = new ConcurrentHashMap<>();

    /** 모든 IP를 합친 1분 한도. IP를 바꿔 가며 누르는 경우를 막는다. */
    private static final String ALL = "*";

    /** 이 IP와 전체가 이번 1분 안에 한도를 넘지 않았으면 true. */
    public boolean tryLogin(String ip) {
        long minute = clock.millis() / 60_000L;
        if (loginWindows.size() > 10_000) {
            loginWindows.entrySet().removeIf(e -> e.getValue().window != minute);
        }
        int limit = properties.getPerMinuteLimit();
        return acquire(loginWindows, "ip:" + (ip == null ? "unknown" : ip), minute, limit)
                && acquire(loginWindows, ALL, minute, limit * 6);
    }

    /** 이 테스트 계정이 오늘 한도를 넘지 않았으면 true. */
    public boolean tryAi(Long userId) {
        long day = LocalDate.now(clock).toEpochDay();
        return acquire(aiWindows, userId, day, properties.getAiDailyLimit());
    }

    private static <K> boolean acquire(Map<K, Window> map, K key, long window, int limit) {
        Window w = map.compute(key, (k, old) -> (old == null || old.window != window) ? new Window(window) : old);
        return w.count.incrementAndGet() <= limit;
    }

    private static final class Window {
        final long window;
        final AtomicInteger count = new AtomicInteger();

        Window(long window) { this.window = window; }
    }
}
