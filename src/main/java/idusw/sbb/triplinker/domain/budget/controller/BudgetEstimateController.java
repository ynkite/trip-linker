package idusw.sbb.triplinker.domain.budget.controller;

import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate;
import idusw.sbb.triplinker.domain.budget.service.BudgetEstimateService;
import idusw.sbb.triplinker.domain.plan.service.TripAccessGuard;
import idusw.sbb.triplinker.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 가계부 「예산 근거」 카드 — 공공 자료로 낸 예상 범위 · 근거 · 출처 · 기준일. 가계부와 같이 주인 · 멤버만. */
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class BudgetEstimateController {

    private final BudgetEstimateService budgetEstimateService;
    private final TripAccessGuard tripAccessGuard;

    @GetMapping("/{tripId}/budget-estimate")
    public ResponseEntity<ApiResponse<BudgetEstimate>> estimate(@AuthenticationPrincipal CustomUserDetails user,
                                                                @PathVariable Long tripId) {
        tripAccessGuard.requireMember(tripId, user != null ? user.getUserId() : null);
        return ResponseEntity.ok(ApiResponse.success(budgetEstimateService.estimate(tripId)));
    }
}
