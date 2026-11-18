package idusw.sbb.triplinker.domain.route.service;

import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 고정 경로 모드.
 *
 * 켜져 있으면 경로 생성이 AI를 전혀 호출하지 않고, 이미 DB에 있는 특정 여행 경로를
 * 그대로 복사해서 보여준다. AI 호출을 챗봇 하나로만 한정해 토큰 비용을 없애기 위한 장치다.
 *
 * 원본 경로는 파일로 따로 보관하지 않는다. 운영 DB의 해당 플랜을 그대로 읽어 쓰므로
 * 내보내기·재배포 없이 원본 플랜만 수정하면 모든 사용자에게 반영된다.
 *
 * 설정(환경변수):
 *   APP_DEMO_ROUTE_ENABLED    true/false. false로 재시작하면 실시간 AI 생성으로 돌아간다.
 *   APP_DEMO_ROUTE_SOURCE_ID  원본 플랜 ID를 직접 지정. 0이면 아래 키워드로 자동 탐색한다.
 *   APP_DEMO_ROUTE_KEYWORD    자동 탐색 키워드. 이 말이 여행지에 들어간 플랜 중
 *                             경로가 있고 가장 최근에 수정된 것을 원본으로 삼는다.
 */
@Service
@RequiredArgsConstructor
public class DemoRouteService {

    public static final String BLOCKED_MESSAGE = "API 비용 문제로 교체는 막아뒀습니다";
    public static final String NOTICE_MESSAGE  = "API 비용 문제로 경로는 이미 생성된 경로로 보여집니다";

    private final TravelPlanRepository planRepository;

    @Value("${app.demo.route.enabled:true}")
    private boolean enabled;

    @Value("${app.demo.route.source-id:0}")
    private long sourceId;

    @Value("${app.demo.route.keyword:해운대}")
    private String keyword;

    /** 원본을 매번 조회하지 않도록 한 번 읽어 들고 있는다. */
    private volatile Source cached;

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 대상 플랜에 원본 경로를 그대로 복사하고, 저장된 경로 JSON을 돌려준다.
     *
     * AiRouteService.saveAiRouteToDb()를 일부러 거치지 않는다. 그 안의 카카오 거리 재계산과
     * 당일치기 숙소 제거가 완성된 경로를 다시 건드려서 원본과 달라지기 때문이다.
     */
    @Transactional
    public String applyTo(Long tripId) {
        Source src = resolveSource();

        // 원본 플랜 자신이 대상이면 덮어쓸 필요가 없다.
        if (src.id.equals(tripId)) {
            return src.routeJson;
        }

        TravelPlan plan = planRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("플랜을 찾을 수 없습니다."));

        plan.setRouteJson(src.routeJson);
        // 수정 중 사본이 남아 있으면 그쪽이 먼저 보이므로 반드시 비운다.
        plan.setDraftRouteJson(null);

        // 화면 머리말(여행지·기간)이 경로 내용과 어긋나지 않도록 원본 값으로 맞춘다.
        if (src.title != null)       plan.setTitle(src.title);
        if (src.destination != null) plan.setDestination(src.destination);
        if (src.startDate != null)   plan.setStartDate(src.startDate);
        if (src.endDate != null)     plan.setEndDate(src.endDate);
        plan.setStatus("FIXED");

        planRepository.save(plan);
        return src.routeJson;
    }

    /** 원본 플랜을 찾아 캐시에 담는다. 지정 ID가 있으면 그것을, 없으면 키워드로 최신 것을 고른다. */
    private Source resolveSource() {
        Source local = cached;
        if (local != null) return local;

        TravelPlan found = null;
        if (sourceId > 0) {
            found = planRepository.findById(sourceId).orElse(null);
        }
        if (found == null || isBlank(found.getRouteJson())) {
            List<TravelPlan> hits =
                    planRepository.findDemoSource(keyword, PageRequest.of(0, 1));
            found = hits.isEmpty() ? null : hits.get(0);
        }
        if (found == null || isBlank(found.getRouteJson())) {
            throw new IllegalStateException(
                    "고정 경로로 쓸 원본 플랜을 찾지 못했습니다. "
                            + "여행지에 '" + keyword + "'가 들어가고 경로가 저장된 플랜이 있어야 합니다.");
        }

        local = new Source(found.getId(), found.getRouteJson(), found.getTitle(),
                found.getDestination(), found.getStartDate(), found.getEndDate());
        cached = local;
        return local;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** 원본 플랜에서 복사해 둔 값. 불변으로 들고 있는다. */
    private record Source(Long id, String routeJson, String title,
                          String destination, LocalDate startDate, LocalDate endDate) { }
}
