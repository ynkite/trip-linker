package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.domain.expense.entity.Expense;
import idusw.sbb.triplinker.domain.plan.entity.PlanInputForm;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.planshare.entity.PlanRole;
import idusw.sbb.triplinker.domain.planshare.entity.TripMember;
import idusw.sbb.triplinker.domain.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

/**
 * 접근 제어 시험에 쓰는 가짜 데이터.
 * 회원 1 = 여행 10 의 주인, 회원 2 = 아무 관계 없는 다른 회원, 회원 3 = 여행 10 에 초대된 편집자.
 * 회원 2 는 자기 여행 20 을 가지고 있다(다른 여행의 지출 번호를 섞어 보내는 경우를 재현).
 */
final class TripFixture {

    static final long OWNER = 1L, OTHER = 2L, EDITOR = 3L;
    static final long TRIP = 10L, OTHER_TRIP = 20L, EXPENSE = 100L;

    final User owner = user(OWNER, "owner", "owner@test.local");
    final User other = user(OTHER, "other", "other@test.local");
    final User editor = user(EDITOR, "editor", "editor@test.local");
    final TravelPlan plan = plan(TRIP, owner);
    final TravelPlan otherPlan = plan(OTHER_TRIP, other);
    final Expense expense = expense(EXPENSE, plan, 50_000L);
    final TripMember editorMember = TripMember.builder().id(1L).travelPlan(plan).user(editor).role(PlanRole.EDITOR).build();

    static User user(long id, String username, String email) {
        User u = User.builder().username(username).name(username).email(email)
                .passwordHash("x").role("USER").status("ACTIVE").build();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    static TravelPlan plan(long id, User owner) {
        TravelPlan p = TravelPlan.builder().id(id).user(owner).destination("제주")
                .title("제주 여행").startDate(LocalDate.of(2026, 12, 1)).endDate(LocalDate.of(2026, 12, 3))
                .status("FIXED").routeJson("[]").build();
        PlanInputForm f = PlanInputForm.builder().plan(p).user(owner).companionCount(2)
                .transportType("자차").budget(500_000L).build();
        p.linkInputForm(f);
        return p;
    }

    static Expense expense(long id, TravelPlan plan, long amount) {
        Expense e = Expense.builder().plan(plan).user(plan.getUser()).category("FOOD")
                .description("점심").amount(amount).isEstimated(false).expenseDate(LocalDate.of(2026, 12, 1)).build();
        ReflectionTestUtils.setField(e, "id", id);
        return e;
    }

    /** 요청을 보낸 사람을 정한다. null 이면 로그인 안 한 사람. */
    static void loginAs(Long userId) {
        if (userId == null) {
            SecurityContextHolder.clearContext();
            return;
        }
        CustomUserDetails d = new CustomUserDetails(userId, "USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(d, null, d.getAuthorities()));
    }
}
