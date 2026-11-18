package idusw.sbb.triplinker.domain.plan.service;

import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import idusw.sbb.triplinker.domain.planshare.entity.PlanRole;
import idusw.sbb.triplinker.domain.planshare.repository.TripMemberRepository;
import idusw.sbb.triplinker.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 여행(TravelPlan) 단위 접근 확인을 한 곳에 모은다.
 * - 읽기: 주인 또는 초대된 멤버(역할 무관)
 * - 쓰기: 주인 또는 EDITOR 로 초대된 멤버 (기존 saveInputForm · updatePlanStatus 와 같은 기준)
 * 로그인 없이 여는 공유 링크 읽기(GET 여행 · 일정 · 인원)는 여기를 거치지 않는다.
 */
@Component
@RequiredArgsConstructor
public class TripAccessGuard {

    private final TravelPlanRepository travelPlanRepository;
    private final TripMemberRepository tripMemberRepository;

    public TravelPlan requireMember(Long tripId, Long userId) {
        TravelPlan plan = load(tripId);
        if (isOwner(plan, userId) || (userId != null && tripMemberRepository.existsByTravelPlanIdAndUserId(tripId, userId))) {
            return plan;
        }
        throw new ForbiddenException();
    }

    public TravelPlan requireEditor(Long tripId, Long userId) {
        TravelPlan plan = load(tripId);
        if (isOwner(plan, userId)
                || (userId != null && (tripMemberRepository.existsByTravelPlanIdAndUserIdAndRole(tripId, userId, PlanRole.EDITOR)
                || tripMemberRepository.existsByTravelPlanIdAndUserIdAndRole(tripId, userId, PlanRole.OWNER)))) {
            return plan;
        }
        throw new ForbiddenException();
    }

    private TravelPlan load(Long tripId) {
        if (tripId == null) throw new IllegalArgumentException("여행 번호가 없습니다.");
        return travelPlanRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플랜입니다."));
    }

    private boolean isOwner(TravelPlan plan, Long userId) {
        return userId != null && plan.getUser() != null && Objects.equals(plan.getUser().getId(), userId);
    }
}
