package idusw.sbb.triplinker.domain.budget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.Admission;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.Input;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.LodgingQuote;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Item;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 예산 산식 — 지역 · 박수 · 인원별 기대 범위를 손으로 계산해 둔 값과 맞춘다. */
class BudgetCalculatorTest {

    /** 실제 일정 JSON 꼴: 둘째 날부터 맨 앞에 전날 숙소(체크아웃), 중간 날 맨 끝에 같은 숙소(취침). 2박 3일. */
    static final String ROUTE_2N3D = """
            [
             {"day":1,"budget":"₩0","places":[
               {"type":"food","name":"식당A","sub":"맛집 · 점심 · ₩9,000×2"},
               {"transit":"🚗 자차 · 10.0km · 약 15분 · ₩2,000"},
               {"type":"tour","name":"관광지A","sub":"관광지 · 1h · ₩0×2"},
               {"transit":"🚗 자차 · 20.5km · 약 25분 · ₩4,100"},
               {"type":"stay","name":"바다호텔","sub":"숙소 · ₩100,000"}]},
             {"day":2,"budget":"₩0","places":[
               {"type":"stay","name":"바다호텔","sub":"숙소 · ₩100,000"},
               {"transit":"🚗 자차 · 5.0km · 약 10분 · ₩1,000"},
               {"type":"tour","name":"관광지B","sub":"관광지 · 1h · ₩3,000×2"},
               {"transit":"🚗 자차 · 4.5km · 약 8분 · ₩900"},
               {"type":"stay","name":"바다호텔","sub":"숙소 · ₩100,000"}]},
             {"day":3,"budget":"₩0","places":[
               {"type":"stay","name":"바다호텔","sub":"숙소 · ₩100,000"},
               {"transit":"🚗 자차 · 10.0km · 약 15분 · ₩2,000"},
               {"type":"cafe","name":"카페C","sub":"카페 · ₩6,000×2"}]}
            ]""";

    static Input input(String sido, int nights, int people, LodgingQuote q, double km) {
        return new Input(sido, null, nights, nights + 1, people, true, q, List.of(), 0, km, 0, 0, null, true);
    }

    /** 그 분류의 계산한 항목(미산정 제외) 첫 줄. */
    static Item item(BudgetEstimate e, String category) {
        return e.items().stream().filter(i -> i.category().equals(category) && i.source() != Source.NOT_ESTIMATED).findFirst().orElseThrow();
    }

    static Item byLabel(BudgetEstimate e, String part) {
        return e.items().stream().filter(i -> i.label().contains(part)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("숙소 줄 4개(체크아웃 2 + 취침 2) 인 2박 일정은 2박으로 센다 — 예전 코드는 4박 값을 더했다")
    void stayNodesAreNotNights() throws Exception {
        RouteFacts f = RouteFacts.parse(ROUTE_2N3D);
        assertEquals(2, f.stayNights());
        assertEquals("바다호텔", f.stayName());
        assertEquals(List.of("관광지A", "관광지B"), f.tours());
        assertEquals(50.0, f.transitKm(), 1e-9);
        assertEquals(10_000, f.transitFare());
        assertEquals(5, f.legs());

        // 예전 셈(숙소 줄마다 1박)이면 4번 — 버그 재현
        JsonNode root = new ObjectMapper().readTree(ROUTE_2N3D);
        int oldCount = 0;
        for (JsonNode d : root) for (JsonNode p : d.path("places")) if ("stay".equals(p.path("type").asText())) oldCount++;
        assertEquals(4, oldCount);
    }

    @Test
    @DisplayName("체크아웃은 둘째 날부터 그날 첫 장소인 숙소만 — 첫날 밤 · 중간 날 밤은 1박")
    void checkoutRuleOnlyFirstPlace() throws Exception {
        JsonNode root = new ObjectMapper().readTree(ROUTE_2N3D);
        JsonNode d1 = root.get(0).path("places"), d2 = root.get(1).path("places");
        assertFalse(RouteFacts.isCheckout(1, d1, 4), "첫날 밤 숙소는 1박");
        assertTrue(RouteFacts.isCheckout(2, d2, 0), "둘째 날 맨 앞 숙소는 체크아웃");
        assertFalse(RouteFacts.isCheckout(2, d2, 4), "둘째 날 밤 숙소는 1박");
        JsonNode lead = new ObjectMapper().readTree("[{\"transit\":\"이동\"},{\"type\":\"stay\",\"name\":\"H\"}]");
        assertTrue(RouteFacts.isCheckout(3, lead, 1), "앞에 이동 줄만 있으면 여전히 첫 장소");
        assertEquals(RouteFacts.EMPTY, RouteFacts.parse("깨진 JSON"));
    }

    @Test
    @DisplayName("제주 2박 2인 자차 · 표본 숙박 6~12만 · 관광지 무료 1 + 2~3천 1 + 모름 1 · 100km · 서울 출발")
    void jejuTwoNightsTwoPeople() {
        LodgingQuote q = new LodgingQuote(60_000, 120_000, Source.STAT_ESTIMATE, "표본", "관광공사", "2026-11-09 조회");
        Input in = new Input("제주", "서울", 2, 3, 2, true, q,
                List.of(new Admission("무료곳", 0, 0, Source.PUBLIC_RATE, "무료"),
                        new Admission("유료곳", 2_000, 3_000, Source.PUBLIC_RATE, "성인 3,000원")),
                1, 100.0, 0, 6, 500_000L, true);
        BudgetEstimate e = BudgetCalculator.compute(in);

        Item stay = item(e, "STAY");
        assertEquals(120_000, stay.min());           // 6만 × 방1 × 2박
        assertEquals(240_000, stay.max());
        assertEquals(Source.STAT_ESTIMATE, stay.source());
        Item food = item(e, "FOOD");
        assertEquals(9_625L * 7 * 2, food.min());     // 제주 백반 × 7끼(3일×3−2) × 2명 = 134,750
        assertEquals(19_250L * 7 * 2, food.max());    // × 2배 = 269,500
        assertEquals(Source.ASSUMED, food.source(), "원문 미확인 통계는 가정");
        Item car = byLabel(e, "현지 이동");
        assertEquals(Math.round(100 / 14.0 * 1_855), car.min());   // 13,250
        assertEquals(Math.round(100 / 9.0 * 1_855), car.max());    // 20,611
        Item tour = item(e, "TOUR");
        assertEquals(4_000, tour.min());
        assertEquals(6_000, tour.max());
        assertEquals(Source.NOT_ESTIMATED, byLabel(e, "요금 모름 1곳").source());
        Item air = byLabel(e, "서울 ↔ 제주 왕복");
        assertEquals(Source.ASSUMED, air.source(), "왕복 교통은 현지 이동과 따로, 항공 단가 추정");
        assertEquals(30_000L * 2 * 2, air.min());           // 편도 1인 3~10만 × 2명 × 왕복
        assertEquals(100_000L * 2 * 2, air.max());
        assertTrue(air.label().contains("추정") && air.basis().startsWith("추정"));

        assertEquals(120_000 + 134_750 + 13_250 + 4_000 + 120_000, e.min());       // 392,000 — 미산정은 합계 제외
        assertEquals(240_000 + 269_500 + 20_611 + 6_000 + 400_000, e.max());       // 936,111
        assertEquals(1, e.missing());
        // 관광공사 자료 185,000 ÷ 계산한 항목 가운데 합 664,055.5 = 27.86% → 27
        assertEquals(27, e.publicShare());
        assertEquals("낮음", e.confidence());
        assertEquals("정한 예산 500,000원이 예상 범위 안에 있습니다. (미산정 1개 제외)", e.budgetCheck());
    }

    @Test
    @DisplayName("응답 JSON 에 범위 · 상태 이름 · 출처 · 기준일이 실린다(화면 시험 입력으로 파일에 남긴다)")
    void jsonCarriesBasisForScreen() throws Exception {
        LodgingQuote q = new LodgingQuote(60_000, 120_000, Source.STAT_ESTIMATE, "같은 지역 숙소 9곳 비수기 주말 최저요금 가운데 50%",
                "한국관광공사 TourAPI 숙박 요금", "2026-11-09 조회");
        BudgetEstimate e = BudgetCalculator.compute(new Input("제주", "서울", 2, 3, 2, true, q,
                List.of(new Admission("한라산", 0, 0, Source.PUBLIC_RATE, "관광공사 이용요금 「무료」")),
                1, 100.0, 0, 6, 500_000L, true));
        String json = new ObjectMapper().writeValueAsString(e);
        assertTrue(json.contains("\"sourceLabel\":\"통계 기반 추정\""), json);
        assertTrue(json.contains("\"sourceLabel\":\"미산정\""));
        assertTrue(json.contains("\"asOf\":\"2026-11-08\""), "유가 기준일");
        java.nio.file.Path out = java.nio.file.Path.of("build", "budget-sample-jeju.json");
        java.nio.file.Files.createDirectories(out.getParent());
        java.nio.file.Files.writeString(out, json);
    }

    @Test
    @DisplayName("부산 1박 4인 대중교통 · 공개 요금 7~9만 → 방 2개, 일정 운임 7,600원")
    void busanOneNightFourPeopleTwoRooms() {
        LodgingQuote q = new LodgingQuote(70_000, 90_000, Source.PUBLIC_RATE, "객실 3개", "관광공사", "2026-11-09 조회");
        Input in = new Input("부산", null, 1, 2, 4, false, q, List.of(), 0, 12.0, 7_600, 3, null, true);
        BudgetEstimate e = BudgetCalculator.compute(in);
        assertEquals(140_000, item(e, "STAY").min());
        assertEquals(180_000, item(e, "STAY").max());
        assertEquals(8_429L * 4 * 4, item(e, "FOOD").min());     // 2일 = 4끼
        assertEquals(16_858L * 4 * 4, item(e, "FOOD").max());
        Item t = item(e, "TRANSPORT");
        assertEquals(7_600, t.min());                             // 기본요금 3구간×4명=18,000 보다 일정 운임이 작다
        assertEquals(18_000, t.max());
        assertEquals(140_000 + 134_864 + 7_600, e.min());
        assertEquals(180_000 + 269_728 + 18_000, e.max());
        assertEquals(0, e.missing());
        assertTrue(e.items().stream().noneMatch(x -> x.label().contains("↔")), "출발지 모르면 왕복 줄 없음");
        assertEquals("정한 예산이 없어 비교하지 않았습니다.", e.budgetCheck());
    }

    @Test
    @DisplayName("서울 당일치기 2인 — 숙박 항목 없음, 2끼, 이동 거리 없으면 유류비 미산정")
    void dayTripHasNoLodging() {
        BudgetEstimate e = BudgetCalculator.compute(input("서울", 0, 2, null, 0));
        assertTrue(e.items().stream().noneMatch(i -> i.category().equals("STAY")));
        assertEquals(8_654L * 2 * 2, item(e, "FOOD").min());
        assertEquals(Source.NOT_ESTIMATED, byLabel(e, "현지 이동").source());
        assertEquals(34_616, e.min());
        assertEquals(69_232, e.max());
        assertEquals(1, e.missing());
    }

    @Test
    @DisplayName("6인 3박 강원 — 방 3개 × 3박, 모르는 시도 식비는 세 시도 범위")
    void sixPeopleNeedThreeRooms() {
        LodgingQuote q = new LodgingQuote(80_000, 100_000, Source.STAT_ESTIMATE, "표본", "관광공사", "-");
        BudgetEstimate e = BudgetCalculator.compute(input("강원", 3, 6, q, 80));
        assertTrue(item(e, "STAY").label().contains("방 3개"));
        assertEquals(80_000L * 3 * 3, item(e, "STAY").min());
        assertEquals(100_000L * 3 * 3, item(e, "STAY").max());
        assertEquals(8_429L * 10 * 6, item(e, "FOOD").min());    // 4일 = 10끼
        assertEquals(19_250L * 10 * 6, item(e, "FOOD").max());
        assertEquals(3, BudgetCalculator.rooms(5));
        assertEquals(1, BudgetCalculator.rooms(1));
        assertEquals(2, BudgetCalculator.mealCount(1));
    }

    private static Input trip(String dep, String dest, int people, boolean car) {
        return new Input(dest, dep, 0, 1, people, car, null, List.of(), 0, 0, 0, 0, null, false);
    }

    @Test
    @DisplayName("서울 → 부산 2인 대중교통 왕복 — 도로 거리 × 고속버스 60원 ~ KTX 150원, 키 없이 추정")
    void seoulBusanTransitRoundTrip() {
        double km = IntercityFare.roadKm("서울", "부산");
        assertTrue(km > 380 && km < 400, "서울~부산 도로 거리 ≈ 390km: " + km);
        Item t = byLabel(BudgetCalculator.compute(trip("서울", "부산", 2, false)), "서울 ↔ 부산 왕복");
        assertEquals(Math.round(km * 60) * 2 * 2, t.min());      // 고속버스 일반 ≈ 서울~부산 2만 3천원대
        assertEquals(Math.round(km * 150) * 2 * 2, t.max());     // KTX ≈ 5만 8천원대
        assertEquals(Source.ASSUMED, t.source());
        assertTrue(t.min() > 90_000 && t.max() < 240_000, t.min() + "~" + t.max());
    }

    @Test
    @DisplayName("서울 → 부산 자차 왕복 — 유류비(연비 9~14) ~ 유류비 + 통행료, 사람 수와 무관")
    void seoulBusanCarRoundTrip() {
        double km = IntercityFare.roadKm("서울", "부산");
        Item a = byLabel(BudgetCalculator.compute(trip("서울", "부산", 2, true)), "왕복");
        Item b = byLabel(BudgetCalculator.compute(trip("서울", "부산", 5, true)), "왕복");
        assertEquals(Math.round(km * 2 / 14.0 * 1_855), a.min());
        assertEquals(Math.round(km * 2 / 9.0 * 1_855) + Math.round(km * 2 * 45), a.max());
        assertEquals(a.min(), b.min());
        assertEquals(a.max(), b.max());
    }

    @Test
    @DisplayName("왕복 교통 줄이 없는 경우 — 같은 시도 · 출발지 모름 · 모르는 시도")
    void noRoundTripLine() {
        for (Input in : List.of(trip("서울", "서울", 2, false), trip(null, "부산", 2, false), trip("서울", null, 2, false)))
            assertTrue(BudgetCalculator.compute(in).items().stream().noneMatch(x -> x.label().contains("↔")));
        assertEquals(-1, IntercityFare.roadKm("서울", "평양"));
        assertEquals(IntercityFare.roadKm("서울", "부산"), IntercityFare.roadKm("부산", "서울"));
    }

    @Test
    @DisplayName("가까운 시도 대중교통은 편도 3천원 아래로 내려가지 않는다 (서울 → 인천)")
    void nearbyTransitHasFloor() {
        Item t = byLabel(BudgetCalculator.compute(trip("서울", "인천", 1, false)), "왕복");
        assertTrue(t.min() >= 3_000L * 2, "" + t.min());
        assertTrue(t.max() >= t.min());
    }

    @Test
    @DisplayName("키 없음 — 숙박 · 입장료 미산정(0원 · 임의 범위로 메우지 않음), 안내는 쉬운 말(설정 이름은 화면에 안 냄), 신뢰도 낮음")
    void noKeyFallsBackToAssumption() {
        Input in = new Input("제주", null, 2, 3, 2, true, null, List.of(), 3, 40, 0, 4, 300_000L, false);
        BudgetEstimate e = BudgetCalculator.compute(in);
        Item stay = byLabel(e, "숙박");
        assertEquals(Source.NOT_ESTIMATED, stay.source());
        assertEquals(0, stay.min());
        assertTrue(stay.basis().contains("관광공사"));
        assertFalse(stay.basis().contains("TOUR_API_KEY"));
        assertEquals(Source.NOT_ESTIMATED, byLabel(e, "요금 모름 3곳").source());
        assertFalse(e.tourApi());
        assertTrue(e.notes().get(0).contains("관광공사"));
        assertFalse(String.join(" ", e.notes()).contains("TOUR_API_KEY"));
        assertFalse(e.items().stream().anyMatch(i -> i.basis().contains("TOUR_API_KEY")));
        assertEquals(0, e.publicShare());
        assertEquals("낮음", e.confidence());
        assertEquals(2, e.missing());
        assertEquals(134_750 + Math.round(40 / 14.0 * 1_855), e.min());   // 식비 + 유류비만
        assertTrue(e.budgetCheck().endsWith("(미산정 2개 제외)"));
    }

    @Test
    @DisplayName("사용자 예산은 계산에 넣지 않는다 — 예산 10만이든 1,000만이든 범위가 같다")
    void userBudgetIsNotUsedInCalculation() {
        LodgingQuote q = new LodgingQuote(60_000, 90_000, Source.PUBLIC_RATE, "객실", "관광공사", "-");
        BudgetEstimate small = BudgetCalculator.compute(new Input("부산", null, 1, 2, 2, true, q, List.of(), 0, 30, 0, 2, 100_000L, true));
        BudgetEstimate big = BudgetCalculator.compute(new Input("부산", null, 1, 2, 2, true, q, List.of(), 0, 30, 0, 2, 10_000_000L, true));
        assertEquals(small.min(), big.min());
        assertEquals(small.max(), big.max());
        assertTrue(small.budgetCheck().contains("적습니다"), small.budgetCheck());
        assertTrue(big.budgetCheck().contains("많습니다"), big.budgetCheck());
    }

    @Test
    @DisplayName("관광공사 공개 요금 숙박 + 확인된 입장료만이면 신뢰도 높음, 미산정이 하나라도 있으면 높음이 아니다")
    void confidenceNeedsLiveDataAndNoGaps() {
        LodgingQuote q = new LodgingQuote(300_000, 400_000, Source.PUBLIC_RATE, "객실", "관광공사", "-");
        BudgetEstimate full = BudgetCalculator.compute(new Input("서울", null, 2, 3, 2, true, q, List.of(), 0, 20, 0, 3, null, true));
        assertTrue(full.publicShare() >= 70, "숙박이 크면 비율이 높다: " + full.publicShare());
        assertEquals("높음", full.confidence());
        BudgetEstimate gap = BudgetCalculator.compute(new Input("서울", null, 2, 3, 2, true, q, List.of(), 1, 20, 0, 3, null, true));
        assertEquals("보통", gap.confidence());
    }

    @Test
    @DisplayName("모르는 지역 — 식비는 서울 · 부산 · 제주 값의 최소~최대 범위")
    void unknownRegionUsesRangeOfThree() {
        BudgetEstimate e = BudgetCalculator.compute(input(null, 0, 1, null, 10));
        assertEquals(8_429L * 2, item(e, "FOOD").min());
        assertEquals(19_250L * 2, item(e, "FOOD").max());
    }

    @Test
    @DisplayName("지역 이름 → 시도 → 관광공사 areaCode")
    void regionCodeNormalizes() {
        assertEquals("제주", RegionCode.sido("제주 제주시"));
        assertEquals("부산", RegionCode.sido("부산광역시 해운대구"));
        assertEquals("경북", RegionCode.sido("경상북도 경주시"));
        assertEquals("전북", RegionCode.sido("전북특별자치도 전주시"));
        assertEquals("강원", RegionCode.sido("강원도 속초시"));
        assertNull(RegionCode.sido("도쿄"));
        assertEquals("39", RegionCode.areaCode("제주"));
        assertEquals("6", RegionCode.areaCode("부산"));
        assertNull(RegionCode.areaCode(null));
    }
}
