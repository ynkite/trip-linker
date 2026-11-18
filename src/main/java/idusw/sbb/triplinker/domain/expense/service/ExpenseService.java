package idusw.sbb.triplinker.domain.expense.service;

import idusw.sbb.triplinker.domain.expense.dto.BudgetReportResponseDto;
import idusw.sbb.triplinker.domain.expense.dto.BudgetReportResponseDto.CategoryBudgetDto;
import idusw.sbb.triplinker.domain.expense.dto.ExpenseAddRequestDto;
import idusw.sbb.triplinker.domain.expense.entity.Expense;
import idusw.sbb.triplinker.domain.expense.repository.ExpenseRepository;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import idusw.sbb.triplinker.domain.plan.service.TripAccessGuard;
import idusw.sbb.triplinker.global.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final TripAccessGuard tripAccessGuard;

    // 가계부는 그 여행의 주인 · 초대된 멤버만 본다 (전에는 로그인만 확인해 번호만 바꾸면 남의 가계부가 보였다)
    public BudgetReportResponseDto getBudgetReport(Long userId, Long planId) {
        TravelPlan plan = tripAccessGuard.requireMember(planId, userId);

        Long budget = (plan.getForm() != null) ? plan.getForm().getBudget() : null;

        List<Expense> expenses = expenseRepository.findByPlanId(planId);

        long totalEst = 0L;
        long totalAct = 0L;
        Map<String, CategoryBudgetDto> categoryMap = new HashMap<>();
        List<BudgetReportResponseDto.ExpenseDetailDto> estimatedExpenses = new ArrayList<>();
        List<BudgetReportResponseDto.ExpenseDetailDto> actualExpenses    = new ArrayList<>();

        for (Expense expense : expenses) {
            String category = expense.getCategory();
            long   amount   = expense.getAmount();

            categoryMap.putIfAbsent(category, CategoryBudgetDto.builder()
                    .category(category)
                    .estimatedAmount(0L)
                    .actualAmount(0L)
                    .build());

            CategoryBudgetDto categoryDto = categoryMap.get(category);

            BudgetReportResponseDto.ExpenseDetailDto detail = BudgetReportResponseDto.ExpenseDetailDto.builder()
                    .id(expense.getId())
                    .category(category)
                    .description(expense.getDescription())
                    .amount(amount)
                    .date(expense.getExpenseDate().toString())
                    .build();

            if (expense.isEstimated()) {
                categoryDto.addEstimated(amount);
                totalEst += amount;
                estimatedExpenses.add(detail);
            } else {
                categoryDto.addActual(amount);
                totalAct += amount;
                actualExpenses.add(detail);
            }
        }

        return BudgetReportResponseDto.builder()
                .budget(budget)
                .tripTitle(plan.getTitle())
                .destination(plan.getDestination())
                .startDate(plan.getStartDate() != null ? plan.getStartDate().toString() : null)
                .endDate(plan.getEndDate()   != null ? plan.getEndDate().toString()   : null)
                .totalEstimatedAmount(totalEst)
                .totalActualAmount(totalAct)
                .categoryBudgets(new ArrayList<>(categoryMap.values()))
                .estimatedExpenses(estimatedExpenses)
                .actualExpenses(actualExpenses)
                .build();
    }

    @Transactional
    public void addExpense(Long userId, Long planId, ExpenseAddRequestDto dto) {
        TravelPlan plan = tripAccessGuard.requireEditor(planId, userId);

        Expense expense = Expense.builder()
                .plan(plan)
                .user(plan.getUser())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .isEstimated(false)
                .expenseDate(dto.getExpenseDate() != null ? dto.getExpenseDate() : LocalDate.now())
                .build();

        expenseRepository.save(expense);
    }

    @Transactional
    public void updateExpense(Long userId, Long planId, Long expenseId, ExpenseAddRequestDto dto) {
        Expense expense = findOwnedExpense(userId, planId, expenseId);

        expense.update(
                dto.getCategory(),
                dto.getDescription(),
                dto.getAmount(),
                dto.getExpenseDate() != null ? dto.getExpenseDate() : expense.getExpenseDate()
        );
    }

    //실제 지출 삭제
    @Transactional
    public void deleteExpense(Long userId, Long planId, Long expenseId) {
        Expense expense = findOwnedExpense(userId, planId, expenseId);
        expenseRepository.delete(expense);
    }

    // 주소의 여행에 편집 권한이 있고, 그 지출이 정말 그 여행 것인지까지 본다
    // (자기 여행 번호에 남의 지출 번호를 섞어 보내는 경우를 막음)
    private Expense findOwnedExpense(Long userId, Long planId, Long expenseId) {
        tripAccessGuard.requireEditor(planId, userId);
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("지출 항목을 찾을 수 없습니다."));
        if (expense.getPlan() == null || !planId.equals(expense.getPlan().getId())) {
            throw new ForbiddenException();
        }
        return expense;
    }
}
