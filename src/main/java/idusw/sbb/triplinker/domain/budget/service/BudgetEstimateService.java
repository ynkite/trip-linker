package idusw.sbb.triplinker.domain.budget.service;

import idusw.sbb.triplinker.domain.budget.BudgetCalculator;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.Admission;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.LodgingQuote;
import idusw.sbb.triplinker.domain.budget.RegionCode;
import idusw.sbb.triplinker.domain.budget.RouteFacts;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate;
import idusw.sbb.triplinker.domain.plan.entity.PlanInputForm;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 여행 한 건의 예상 예산 — 일정 · 입력값을 모으고(여기서만 외부 API 를 탄다) 계산은 BudgetCalculator 에 맡긴다.
 * 관광공사가 느리거나 죽어도 예외를 밖으로 내지 않는다. 그 항목만 「가정」 으로 내려간다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetEstimateService {

    /** 관광지 요금 조회 상한 — 한 번 계산에 관광공사 호출이 수십 건으로 늘지 않게. 넘는 곳은 「요금 모름」. */
    static final int MAX_TOUR_LOOKUPS = 8;

    private final TravelPlanRepository planRepository;
    private final TourPriceService tourPriceService;

    public BudgetEstimate estimate(Long planId) {
        TravelPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("여행 플랜을 찾을 수 없습니다."));
        return estimate(plan);
    }

    public BudgetEstimate estimate(TravelPlan plan) {
        PlanInputForm form = plan.getForm();
        String sido = RegionCode.sido(plan.getDestination());
        String area = RegionCode.areaCode(sido);
        LocalDate start = plan.getStartDate() != null ? plan.getStartDate() : LocalDate.now();
        LocalDate end = plan.getEndDate() != null && !plan.getEndDate().isBefore(start) ? plan.getEndDate() : start;
        int nights = (int) ChronoUnit.DAYS.between(start, end);
        int people = form != null && form.getCompanionCount() != null && form.getCompanionCount() > 0 ? form.getCompanionCount() : 2;
        boolean car = form == null || form.getTransportType() == null || !form.getTransportType().contains("대중교통");

        String draft = plan.getDraftRouteJson();
        RouteFacts facts = RouteFacts.parse(draft != null && !draft.isBlank() ? draft : plan.getRouteJson());

        LodgingQuote lodging = null;
        if (nights > 0) {
            try {
                lodging = tourPriceService.lodging(facts.stayName(), area, start);
            } catch (Exception e) {
                log.warn("[budget] 숙박 요금 조회 실패: {}", e.getMessage());
            }
        }

        List<Admission> known = new ArrayList<>();
        int unknown = 0;
        for (int i = 0; i < facts.tours().size(); i++) {
            Admission a = null;
            if (i < MAX_TOUR_LOOKUPS) {
                try {
                    a = tourPriceService.admission(facts.tours().get(i), area);
                } catch (Exception e) {
                    log.warn("[budget] 이용요금 조회 실패: {}", e.getMessage());
                }
            }
            if (a != null) known.add(a); else unknown++;
        }

        return BudgetCalculator.compute(new BudgetCalculator.Input(
                sido, form != null ? RegionCode.sido(form.getDeparture()) : null,
                nights, nights + 1, people, car,
                lodging, known, unknown,
                facts.transitKm(), facts.transitFare(), facts.legs(),
                form != null ? form.getBudget() : null,
                tourPriceService.enabled()));
    }
}
