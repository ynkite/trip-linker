package idusw.sbb.triplinker.domain.budget.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 한국관광공사 TourAPI(KorService2) 호출기.
 *
 * - 키가 없으면 호출하지 않고 빈 결과를 돌려준다. 예산 계산은 「가정」 값으로 내려갈 뿐 화면은 뜬다.
 * - 연결 3초 · 응답 5초 제한, 실패하면 한 번 더. 그래도 실패하면 빈 결과.
 * - 같은 요청은 1시간 메모리 캐시. 개발계정 하루 1,000건 한도를 아끼고, 응답을 DB 에 오래 쌓지 않는다.
 * - 디코딩 키를 넣는다. 인코딩 키를 넣으면 SERVICE_KEY_IS_NOT_REGISTERED_ERROR.
 */
@Slf4j
@Component
public class TourApiClient {

    private static final String SERVICE = "KorService2";
    private static final long CACHE_TTL_MS = 60 * 60_000L;

    private final String key;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();
    private final AtomicInteger httpCalls = new AtomicInteger();

    private record Cached(JsonNode items, long at) {}

    public TourApiClient(@Value("${tour.api.key:${TOUR_API_KEY:}}") String key,
                         @Value("${tour.api.base-url:https://apis.data.go.kr/B551011}") String baseUrl) {
        this.key = key == null ? "" : key.trim();
        this.baseUrl = baseUrl;
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(3000);
        f.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(f);
    }

    /** 키가 들어 있는지. 없으면 관광공사 자료 없이 계산한다. */
    public boolean enabled() {
        return !key.isEmpty();
    }

    /** 실제로 나간 HTTP 호출 수 (시험 · 한도 확인용). */
    public int httpCalls() {
        return httpCalls.get();
    }

    /**
     * operation 을 부르고 response.body.items.item 을 돌려준다.
     * 결과가 하나면 관광공사는 배열이 아니라 객체로 준다 → 부르는 쪽이 {@link #list} 로 편다.
     * 키 없음 · 실패 · 오류코드면 Optional.empty().
     */
    public Optional<JsonNode> items(String operation, Map<String, String> params) {
        if (!enabled()) return Optional.empty();

        Map<String, String> sorted = new TreeMap<>(params);
        String cacheKey = operation + sorted;
        Cached c = cache.get(cacheKey);
        if (c != null && System.currentTimeMillis() - c.at() < CACHE_TTL_MS) return Optional.of(c.items());

        StringBuilder qs = new StringBuilder("?serviceKey=").append(enc(key))
                .append("&MobileOS=ETC&MobileApp=triplinker&_type=json");
        sorted.forEach((k, v) -> qs.append('&').append(k).append('=').append(enc(v)));
        String raw = get(URI.create(baseUrl + "/" + SERVICE + "/" + operation + qs), operation);
        if (raw == null) return Optional.empty();

        try {
            JsonNode root = objectMapper.readTree(raw);
            String code = root.path("response").path("header").path("resultCode").asText();
            if (!"0000".equals(code)) {
                log.warn("[tour] {} 오류코드 {}", operation, code);
                return Optional.empty();
            }
            JsonNode items = root.path("response").path("body").path("items").path("item");
            JsonNode out = items.isMissingNode() ? objectMapper.createArrayNode() : items;
            cache.put(cacheKey, new Cached(out, System.currentTimeMillis()));
            return Optional.of(out);
        } catch (Exception e) {
            log.warn("[tour] {} 응답 해석 실패: {}", operation, e.getMessage());
            return Optional.empty();
        }
    }

    /** 배열 · 객체 · 없음을 모두 목록으로. */
    public static java.util.List<JsonNode> list(Optional<JsonNode> node) {
        java.util.List<JsonNode> out = new java.util.ArrayList<>();
        node.ifPresent(n -> {
            if (n.isObject()) out.add(n);
            else n.forEach(out::add);
        });
        return out;
    }

    private String get(URI uri, String operation) {
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                httpCalls.incrementAndGet();
                return restTemplate.getForObject(uri, String.class);
            } catch (Exception e) {
                log.warn("[tour] {} 호출 실패 ({}차): {}", operation, attempt, e.getMessage());
            }
        }
        return null;
    }

    private static String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }
}
