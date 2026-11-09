package idusw.sbb.triplinker.domain.budget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.Admission;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.LodgingQuote;
import idusw.sbb.triplinker.domain.budget.client.TourApiClient;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;
import idusw.sbb.triplinker.domain.budget.service.TourPriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** 관광공사 응답 꼴(가짜)로 숙박 요금 · 이용요금 고르기를 본다. 네트워크는 쓰지 않는다. */
class TourPriceServiceTest {

    static final ObjectMapper OM = new ObjectMapper();

    /** operation|contentId 또는 operation|keyword → items JSON */
    static TourApiClient fake(Map<String, String> answers) {
        return new TourApiClient("test-key", "http://unused") {
            @Override
            public Optional<JsonNode> items(String operation, Map<String, String> params) {
                String k = operation + "|" + params.getOrDefault("contentId", params.getOrDefault("keyword", params.getOrDefault("areaCode", "")));
                String json = answers.get(k);
                try {
                    return json == null ? Optional.of(OM.createArrayNode()) : Optional.of(OM.readTree(json));
                } catch (Exception e) {
                    return Optional.empty();
                }
            }
        };
    }

    static String rooms(long... weekdayOff) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < weekdayOff.length; i++) {
            if (i > 0) sb.append(',');
            sb.append("{\"roomoffseasonminfee1\":\"").append(weekdayOff[i]).append("\",\"roompeakseasonminfee2\":\"0\"}");
        }
        return sb.append(']').toString();
    }

    @Test
    @DisplayName("일정 숙소의 객실 요금이 있으면 가장 싼 방 ~ 가장 비싼 방(공개 요금)")
    void lodgingUsesStayRoomFees() {
        Map<String, String> a = new HashMap<>();
        a.put("searchKeyword2|바다호텔", "{\"contentid\":\"100\",\"title\":\"바다호텔\"}");   // 1건이면 객체로 온다
        a.put("detailInfo2|100", rooms(0, 85_000, 70_000, 120_000));
        TourPriceService s = new TourPriceService(fake(a));
        // 2026-11-10 화요일 = 비수기 주중 → roomoffseasonminfee1
        LodgingQuote q = s.lodging("바다호텔", "39", LocalDate.of(2026, 11, 10));
        assertNotNull(q);
        assertEquals(Source.PUBLIC_RATE, q.source());
        assertEquals(70_000, q.minPerRoom());
        assertEquals(120_000, q.maxPerRoom());
        assertTrue(q.basis().contains("비수기 주중"));
    }

    @Test
    @DisplayName("요금 칸이 비면 같은 지역 표본 → 4곳 이상이면 가운데 50%(25~75 백분위)")
    void lodgingFallsBackToRegionalSampleMiddleHalf() {
        Map<String, String> a = new HashMap<>();
        a.put("searchKeyword2|빈요금호텔", "[{\"contentid\":\"1\"}]");
        a.put("detailInfo2|1", rooms(0));
        StringBuilder list = new StringBuilder("[");
        long[] mins = {40_000, 300_000, 55_000, 90_000, 70_000, 65_000, 80_000, 60_000};
        for (int i = 0; i < mins.length; i++) {
            list.append(i > 0 ? "," : "").append("{\"contentid\":\"s").append(i).append("\"}");
            a.put("detailInfo2|s" + i, rooms(mins[i], mins[i] + 30_000));
        }
        a.put("areaBasedList2|39", list.append(']').toString());
        LodgingQuote q = new TourPriceService(fake(a)).lodging("빈요금호텔", "39", LocalDate.of(2026, 11, 10));
        assertNotNull(q);
        assertEquals(Source.STAT_ESTIMATE, q.source());
        // 정렬 40,55,60,65,70,80,90,300(천) → 25번째 = 2번째 55,000 · 75번째 = 6번째 80,000. 300,000 같은 튀는 값은 빠진다
        assertEquals(55_000, q.minPerRoom());
        assertEquals(80_000, q.maxPerRoom());
        assertTrue(q.basis().contains("8곳"));
    }

    @Test
    @DisplayName("여름 금요일 체크인은 성수기 주말 칸, 자료가 전혀 없으면 null(→ 가정)")
    void peakWeekendFieldAndNothingFound() {
        Map<String, String> a = new HashMap<>();
        a.put("searchKeyword2|H", "[{\"contentid\":\"7\"}]");
        a.put("detailInfo2|7", "[{\"roomoffseasonminfee1\":\"50000\",\"roompeakseasonminfee2\":\"150000\"}]");
        TourPriceService s = new TourPriceService(fake(a));
        LodgingQuote q = s.lodging("H", null, LocalDate.of(2026, 8, 7));   // 금요일
        assertEquals(150_000, q.minPerRoom());
        assertTrue(q.basis().contains("성수기 주말"));
        assertNull(new TourPriceService(fake(Map.of())).lodging("없는호텔", "39", LocalDate.of(2026, 11, 10)));
    }

    @Test
    @DisplayName("이용요금 글 — 무료면 0, 금액은 가장 싼 값~비싼 값, 금액 · 무료 없으면 null")
    void parseFeeText() throws Exception {
        var m = TourPriceService.class.getDeclaredMethod("parseFee", String.class, String.class);
        m.setAccessible(true);
        Admission free = (Admission) m.invoke(null, "A", "무료");
        assertEquals(0, free.max());
        Admission paid = (Admission) m.invoke(null, "B", "성인 3,000원<br>청소년 2,000원<br>어린이 1,000원");
        assertEquals(1_000, paid.min());
        assertEquals(3_000, paid.max());
        Admission mixed = (Admission) m.invoke(null, "C", "입장 무료 / 체험 5,000원");
        assertEquals(0, mixed.min());
        assertEquals(5_000, mixed.max());
        assertNull(m.invoke(null, "D", "문의 바람"));
        assertNull(m.invoke(null, "E", ""));
    }

    @Test
    @DisplayName("관광지 이름 → 문화시설 usefee 로 이용요금")
    void admissionFromUsefee() {
        Map<String, String> a = new HashMap<>();
        a.put("searchKeyword2|박물관", "[{\"contentid\":\"55\",\"contenttypeid\":\"14\"}]");
        a.put("detailIntro2|55", "{\"usefee\":\"어른 2,000원, 어린이 1,000원\"}");
        Admission ad = new TourPriceService(fake(a)).admission("박물관", "6");
        assertEquals(1_000, ad.min());
        assertEquals(2_000, ad.max());
        assertEquals(Source.PUBLIC_RATE, ad.source());
    }

    @Test
    @DisplayName("백분위(nearest-rank)")
    void percentileNearestRank() throws Exception {
        var m = TourPriceService.class.getDeclaredMethod("percentile", List.class, int.class);
        m.setAccessible(true);
        assertEquals(10L, m.invoke(null, List.of(10L, 20L, 30L, 40L), 25));   // 순위 ceil(0.25×4)=1
        assertEquals(30L, m.invoke(null, List.of(10L, 20L, 30L, 40L), 75));
    }
}
