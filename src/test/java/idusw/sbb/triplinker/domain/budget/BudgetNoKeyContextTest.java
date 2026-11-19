package idusw.sbb.triplinker.domain.budget;

import idusw.sbb.triplinker.domain.budget.client.TourApiClient;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;
import idusw.sbb.triplinker.domain.budget.service.BudgetEstimateService;
import idusw.sbb.triplinker.domain.budget.service.TourPriceService;
import idusw.sbb.triplinker.domain.plan.entity.PlanInputForm;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 키(TOUR_API_KEY) 없이 스프링 빈이 뜨고, 예산이 「가정」 으로 나오며, 관광공사로 HTTP 가 한 번도 안 나가는지.
 * 앱 전체(@SpringBootTest)는 DB 설정이 없는 이 환경에서 못 뜨므로 예산 빈들만 실제 스프링 문맥으로 띄운다.
 */
class BudgetNoKeyContextTest {

    @Test
    @DisplayName("키 없음 — 문맥이 뜨고, 숙박은 가정, 호출 0번, 예외 없음")
    void noKeyContextStartsAndEstimates() {
        TravelPlanRepository repo = Mockito.mock(TravelPlanRepository.class);
        PlanInputForm form = PlanInputForm.builder()
                .departure("서울 강남구").transportType("자차").companionCount(2).budget(400_000L).build();
        TravelPlan plan = TravelPlan.builder()
                .id(1L).destination("제주 제주시")
                .startDate(LocalDate.of(2026, 12, 4)).endDate(LocalDate.of(2026, 12, 6))
                .form(form).routeJson(BudgetCalculatorTest.ROUTE_2N3D).build();
        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(plan));

        new ApplicationContextRunner()
                .withPropertyValues("tour.api.key=")      // 키 없음(환경변수가 있어도 비운다)
                .withBean(TravelPlanRepository.class, () -> repo)
                .withBean(TourApiClient.class)
                .withBean(TourPriceService.class)
                .withBean(PriceResearch.class)
                .withBean(BudgetEstimateService.class)
                .run(ctx -> {
                    assertNull(ctx.getStartupFailure(), "문맥이 떠야 한다");
                    TourApiClient client = ctx.getBean(TourApiClient.class);
                    assertFalse(client.enabled());

                    BudgetEstimate e = ctx.getBean(BudgetEstimateService.class).estimate(1L);

                    assertEquals(0, client.httpCalls(), "키가 없으면 관광공사로 나가지 않는다");
                    assertFalse(e.tourApi());
                    var stay = e.items().stream().filter(i -> i.category().equals("STAY")).findFirst().orElseThrow();
                    assertEquals(Source.NOT_ESTIMATED, stay.source(), "키가 없으면 숙박은 미산정(0원 · 임의 범위 아님)");
                    var car = e.items().stream().filter(i -> i.label().contains("현지 이동")).findFirst().orElseThrow();
                    assertEquals(Source.ASSUMED, car.source(), "일정의 카카오 거리 50km × 유가 가정");
                    assertEquals(Math.round(50 / 14.0 * 1_855), car.min());
                    assertTrue(e.items().stream().anyMatch(i -> i.label().contains("요금 모름 2곳")));
                    assertTrue(e.items().stream().anyMatch(i -> i.label().equals("서울 ↔ 제주 왕복 교통(추정)") && i.source() == Source.ASSUMED));
                    assertTrue(e.notes().get(0).contains("관광공사"));
                    assertFalse(e.notes().get(0).contains("TOUR_API_KEY"));
                    assertEquals(2, e.missing());
                    assertEquals("낮음", e.confidence());
                    assertEquals(9_625L * 7 * 2 + Math.round(50 / 14.0 * 1_855) + 30_000L * 2 * 2, e.min());
                    System.out.println("NOKEY_CALLS=" + client.httpCalls() + " range=" + e.min() + "~" + e.max());
                    java.nio.file.Path out = java.nio.file.Path.of("build", "budget-sample-nokey.json");
                    java.nio.file.Files.createDirectories(out.getParent());
                    java.nio.file.Files.writeString(out, new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(e));
                });
    }
}
