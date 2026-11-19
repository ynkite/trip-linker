package idusw.sbb.triplinker.domain.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.LodgingQuote;
import idusw.sbb.triplinker.domain.budget.PriceResearch;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 예시 일정 네 개의 「예산 근거」 — 관광공사 이용요금이 하나도 없을 때(운영에서 부산 · 대구 · 대전이 그렇다)
 * 조사한 공개 요금 표만으로 입장료가 얼마나 채워지는지, 대전(모두 도보)의 현지 이동이 미산정이 아닌지 본다.
 * 숙박은 운영이 11-19 에 관광공사에서 받은 범위(부산 45,000~350,000 · 속초 70,000~150,000)를 그대로 넣는다 — 네트워크는 쓰지 않는다.
 * 결과는 build/demo-budget-after.json 에 남긴다(화면 확인용).
 */
class DemoSeedBudgetTest {

    static final ObjectMapper OM = new ObjectMapper();

    static int unknownTours(BudgetEstimate e) {
        return e.items().stream().filter(i -> i.label().contains("요금 모름"))
                .mapToInt(i -> Integer.parseInt(i.label().replaceAll("\\D", ""))).sum();
    }

    @Test
    @DisplayName("예시 일정 넷 — 조사표로 입장료 미산정이 줄고, 대전 현지 이동은 도보 0원")
    void demoTrips() throws Exception {
        LocalDate today = LocalDate.of(2026, 11, 19);
        DemoSeedService seed = new DemoSeedService(null, OM);
        PriceResearch research = new PriceResearch();
        Map<String, BudgetEstimate> out = new LinkedHashMap<>();
        for (DemoSeedData.Trip t : DemoSeedData.TRIPS) {
            TravelPlanRepository repo = Mockito.mock(TravelPlanRepository.class);
            PlanInputForm form = PlanInputForm.builder().departure("서울").transportType(t.transport())
                    .companionCount(t.companionCount()).budget(t.budget()).build();
            TravelPlan plan = TravelPlan.builder().id(1L).destination(t.destination())
                    .startDate(t.startDate(today)).endDate(t.endDate(today)).form(form)
                    .routeJson(seed.buildRouteJson(t, t.startDate(today), Map.of())).build();
            Mockito.when(repo.findById(1L)).thenReturn(Optional.of(plan));
            TourPriceService tour = Mockito.mock(TourPriceService.class);
            Mockito.when(tour.enabled()).thenReturn(true);
            Mockito.when(tour.lodging(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(switch (t.key()) {
                case "busan" -> new LodgingQuote(45_000, 350_000, Source.STAT_ESTIMATE,
                        "같은 지역 숙소 3곳 비수기 주중 최저요금 최저~최고", "한국관광공사 TourAPI 숙박 요금", "2026-11-19 조회");
                case "sokcho" -> new LodgingQuote(70_000, 150_000, Source.STAT_ESTIMATE,
                        "같은 지역 숙소 9곳 비수기 주말 최저요금 가운데 50%", "한국관광공사 TourAPI 숙박 요금", "2026-11-19 조회");
                default -> null;
            });
            BudgetEstimate e = new BudgetEstimateService(repo, tour, research).estimate(1L);
            out.put(t.key(), e);
            System.out.println("DEMO_AFTER " + t.key() + " 요금모름=" + unknownTours(e) + " 미산정=" + e.missing());
        }
        Path p = Path.of("build", "demo-budget-after.json");
        Files.createDirectories(p.getParent());
        Files.writeString(p, OM.writerWithDefaultPrettyPrinter().writeValueAsString(out));

        // 운영(11-19, 관광공사만): 부산 6 · 대구 4 · 대전 3곳 요금 모름
        assertEquals(1, unknownTours(out.get("busan")), "동백섬만 남음(조사한 쪽은 누리마루 항목이라 동백섬에 쓰지 않음)");
        assertEquals(2, unknownTours(out.get("daegu")), "청라언덕 · 계산성당");
        assertEquals(2, unknownTours(out.get("daejeon")), "대전중앙시장(시장 조사값 없음) · 소제동 카페거리(거리 조사값 1곳뿐)");
        assertEquals(2, unknownTours(out.get("sokcho")), "영금정 · 속초등대 — 운영에서는 관광공사가 먼저 채운다");

        Item walk = out.get("daejeon").items().stream().filter(i -> i.category().equals("TRANSPORT") && i.label().startsWith("현지"))
                .findFirst().orElseThrow();
        assertNotEquals(Source.NOT_ESTIMATED, walk.source(), "대전 일정은 모든 구간이 도보 — 0원이지 미산정이 아니다");
        assertEquals(0, walk.max());
    }
}
