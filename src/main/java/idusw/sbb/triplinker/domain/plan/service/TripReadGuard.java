package idusw.sbb.triplinker.domain.plan.service;

import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import idusw.sbb.triplinker.domain.planshare.repository.TripMemberRepository;
import idusw.sbb.triplinker.domain.planshare.service.ShareTokenService;
import idusw.sbb.triplinker.domain.post.repository.PostRepository;
import idusw.sbb.triplinker.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 로그인 없이도 열리는 읽기 주소(GET 여행 · 일정 · 인원)의 확인.
 * 다음 가운데 하나면 읽을 수 있다.
 *  1) 주인 또는 초대된 멤버
 *  2) 읽기 전용 공유 링크의 토큰(X-Share-Token 머리글)이 이 여행의 것
 *  3) 이 여행이 커뮤니티에 공개(ACTIVE)된 후기 글의 원본 일정 — 글쓴이가 스스로 공개한 것이라 후기 화면이 원본 일정을 보여 준다
 */
@Component
@RequiredArgsConstructor
public class TripReadGuard {

    public static final String HEADER = "X-Share-Token";

    private final TravelPlanRepository travelPlanRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ShareTokenService shareTokenService;
    private final PostRepository postRepository;

    public void requireReader(Long tripId, Long userId, String shareToken) {
        if (tripId == null) throw new IllegalArgumentException("여행 번호가 없습니다.");
        TravelPlan plan = travelPlanRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플랜입니다."));
        if (userId != null && plan.getUser() != null && Objects.equals(plan.getUser().getId(), userId)) return;
        if (userId != null && tripMemberRepository.existsByTravelPlanIdAndUserId(tripId, userId)) return;
        if (shareTokenService.isValid(tripId, shareToken)) return;
        if (!postRepository.findByPlanIdAndStatusOrderByCreatedAtDesc(tripId, "ACTIVE", Pageable.ofSize(1)).isEmpty()) return;
        throw new ForbiddenException();
    }
}
