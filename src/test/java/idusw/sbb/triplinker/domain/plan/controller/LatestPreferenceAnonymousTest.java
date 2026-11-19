package idusw.sbb.triplinker.domain.plan.controller;

import idusw.sbb.triplinker.domain.plan.service.TravelPlanService;
import idusw.sbb.triplinker.domain.plan.service.TripReadGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * GET /api/trips/latest-preference 는 공유 링크용 「/api/trips/* 로그인 없이 열기」 규칙에 같이 걸려
 * 로그인 안 한 요청도 컨트롤러까지 온다. 그때 서버 오류(500)가 아니라 401 이어야 한다.
 */
class LatestPreferenceAnonymousTest {

    @Test
    @DisplayName("로그인 안 한 요청은 401 이고 서비스를 부르지 않는다")
    void anonymousGets401() {
        TravelPlanService service = mock(TravelPlanService.class);
        TravelPlanController controller = new TravelPlanController(service, mock(TripReadGuard.class));

        var res = controller.getLatestPreference(null);

        assertThat(res.getStatusCode().value()).isEqualTo(401);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().isSuccess()).isFalse();
        verifyNoInteractions(service);
    }
}
