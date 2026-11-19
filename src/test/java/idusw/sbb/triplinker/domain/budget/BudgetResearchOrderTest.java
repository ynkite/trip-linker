package idusw.sbb.triplinker.domain.budget;

import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Item;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;
import idusw.sbb.triplinker.domain.budget.service.BudgetEstimateService;
import idusw.sbb.triplinker.domain.budget.service.TourPriceService;
import idusw.sbb.triplinker.domain.plan.entity.PlanInputForm;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** 입장료 차례: ① 관광공사 → ② 조사한 공개 요금 → ③ 같은 유형 조사값 범위 → ④ 미산정. 관광공사는 가짜 응답. */
class BudgetResearchOrderTest {

    static final String ROUTE = """
            [{"day":1,"places":[
              {"type":"tour","name":"유료곳"},
              {"transit":"🚌 대중교통 · 3.0km · 약 8분 · ₩1,500"},
              {"type":"tour","name":"조사한곳"},
              {"transit":"🚌 대중교통 · 3.0km · 약 8분 · ₩1,500"},
              {"type":"tour","name":"새로생긴해수욕장"},
              {"transit":"🚌 대중교통 · 3.0km · 약 8분 · ₩1,500"},
              {"type":"tour","name":"아무개바위"}]}]""";

    static final String HEAD = "sido,city,name,type,adult_fee,source_name,source_url,checked,quote";

    BudgetEstimate run(PriceResearch research) {
        Map<String, String> a = new HashMap<>();
        a.put("searchKeyword2|유료곳", "{\"contentid\":\"7\",\"contenttypeid\":\"14\"}");
        a.put("detailIntro2|7", "{\"usefee\":\"성인 3,000원\"}");
        TravelPlanRepository repo = Mockito.mock(TravelPlanRepository.class);
        PlanInputForm form = PlanInputForm.builder().transportType("대중교통").companionCount(1).build();
        TravelPlan plan = TravelPlan.builder().id(1L).destination("부산")
                .startDate(LocalDate.of(2026, 12, 4)).endDate(LocalDate.of(2026, 12, 4))
                .form(form).routeJson(ROUTE).build();
        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(plan));
        return new BudgetEstimateService(repo, new TourPriceService(TourPriceServiceTest.fake(a)), research).estimate(1L);
    }

    static Item label(BudgetEstimate e, String part) {
        return e.items().stream().filter(i -> i.label().contains(part)).findFirst().orElseThrow(() -> new AssertionError(part + " 없음: " + e.items()));
    }

    @Test
    @DisplayName("관광공사 값이 있으면 조사값보다 먼저, 없으면 조사값, 유형만 알면 범위, 아무것도 없으면 미산정")
    void order() {
        PriceResearch r = new PriceResearch(List.of(
                "부산,해운대구,유료곳,기타,9999,조사기관,https://x,2026-11-19,9999원",
                "부산,해운대구,조사한곳,기타,5000,부산 어느구청,https://y,2026-11-19,\"성인 5,000원\"",
                "부산,해운대구,해운대해수욕장,해수욕장,0,해운대구청,https://z,2026-11-19,입장료 무료"), HEAD);
        BudgetEstimate e = run(r);

        Item pub = label(e, "요금 확인 1곳");
        assertEquals(Source.PUBLIC_RATE, pub.source());
        assertEquals(3_000, pub.min(), "관광공사 3,000원이 조사표 9,999원보다 먼저");

        Item res = label(e, "요금 조사 1곳");
        assertEquals(5_000, res.min());
        assertEquals("부산 어느구청", res.sourceName());

        Item est = label(e, "유형별 추정 1곳");
        assertEquals(0, est.max());

        assertEquals(Source.NOT_ESTIMATED, label(e, "요금 모름 1곳").source());
    }

    @Test
    @DisplayName("이름이 같아도 지역이 다른 조사값은 쓰지 않는다")
    void otherRegionIgnored() {
        PriceResearch r = new PriceResearch(List.of(
                "서울,종로구,조사한곳,기타,5000,서울 어느구청,https://y,2026-11-19,\"성인 5,000원\""), HEAD);
        BudgetEstimate e = run(r);
        assertTrue(e.items().stream().noneMatch(i -> i.label().contains("요금 조사")));
        assertEquals(Source.NOT_ESTIMATED, label(e, "요금 모름 3곳").source());
    }
}
