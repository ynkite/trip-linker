package idusw.sbb.triplinker.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.expense.repository.ExpenseRepository;
import idusw.sbb.triplinker.domain.place.repository.PlaceRepository;
import idusw.sbb.triplinker.domain.place.service.PlaceService;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import idusw.sbb.triplinker.domain.route.service.AiRouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OWASP LLM Top 10 — 「AI 가 준 값을 검증 없이 믿는가」 재현 시험 (10월 점검 6장의 AI 좌표 신뢰).
 * AI 가 일정 JSON 에 지어낸 장소와 그럴듯한 좌표(lat/lng)를 함께 넣으면,
 * 카카오 장소 검색에서 찾지 못했는데도 그 좌표를 믿어 「좌표 없는 가짜 장소 삭제」 를 통과하던 문제.
 * 카카오 API 는 가짜(RestTemplate 모의)로 바꿔, 성산일출봉만 찾고 나머지는 못 찾게 한다.
 */
class AiCoordinateTrustTest {

    static final long TRIP = 10L;
    final ObjectMapper om = new ObjectMapper();
    TravelPlanRepository planRepo;
    RestTemplate rest;
    AiRouteService service;
    TravelPlan plan;

    @BeforeEach
    void setUp() {
        planRepo = mock(TravelPlanRepository.class);
        rest = mock(RestTemplate.class);
        plan = TravelPlan.builder().id(TRIP).user(TripFixture.user(1L, "owner", "o@test.local"))
                .destination("제주").title("제주 여행")
                .startDate(LocalDate.of(2026, 12, 1)).endDate(LocalDate.of(2026, 12, 2))
                .status("DRAFT").routeJson("[]").build();
        when(planRepo.findById(TRIP)).thenReturn(Optional.of(plan));

        // 카카오 키워드 검색: 「성산일출봉」 만 실제 장소로 찾고, 나머지는 결과 0개. 길찾기 등 다른 호출도 빈 결과.
        when(rest.exchange(any(URI.class), eq(HttpMethod.GET), any(), eq(String.class))).thenAnswer(inv -> {
            String url = URLDecoder.decode(inv.getArgument(0, URI.class).toString(), StandardCharsets.UTF_8);
            if (url.contains("keyword.json") && url.contains("성산일출봉")) {
                return ResponseEntity.ok("{\"documents\":[{\"place_name\":\"성산일출봉\",\"address_name\":\"제주특별자치도 서귀포시 성산읍\",\"y\":\"33.4590\",\"x\":\"126.9425\"}]}");
            }
            return ResponseEntity.ok("{\"documents\":[],\"routes\":[]}");
        });
        when(rest.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class), any(Object[].class)))
                .thenReturn(ResponseEntity.ok("{\"documents\":[],\"routes\":[]}"));

        service = new AiRouteService(planRepo, mock(ExpenseRepository.class), mock(PlaceService.class),
                mock(PlaceRepository.class), om, rest,
                mock(ChatModel.class), mock(ChatModel.class), mock(ChatModel.class));
    }

    /** AI 가 만든 일정: 실제 장소 1 + 지어낸 장소 1. 둘 다 AI 가 좌표를 적어 넣었다. */
    private String aiRoute() {
        return """
            [{"day":1,"label":"Day 1","budget":"₩50,000","places":[
              {"name":"성산일출봉","type":"tour","sub":"관광 · ₩5,000","time":"09:30","lat":33.4590,"lng":126.9425},
              {"transit":"이동"},
              {"name":"성산 바다뷰 비밀카페","type":"cafe","sub":"카페 · ₩7,000","time":"11:00","lat":33.4601,"lng":126.9400}
            ]}]""";
    }

    private List<String> savedNames() throws Exception {
        JsonNode root = om.readTree(plan.getRouteJson());
        List<String> names = new ArrayList<>();
        for (JsonNode p : root.get(0).path("places")) if (p.has("name")) names.add(p.path("name").asText());
        return names;
    }

    @Test
    @DisplayName("카카오에서 못 찾은 장소는 AI 가 좌표를 적어 넣었어도 「좌표 없는 가짜」 로 지운다")
    void fakePlaceWithAiCoordinatesIsRemoved() throws Exception {
        service.postProcessRoute(TRIP, aiRoute(), Set.of(), "보통");

        assertThat(savedNames()).containsExactly("성산일출봉");
    }

    @Test
    @DisplayName("저장된 일정에 AI 가 지어낸 좌표가 남지 않는다(지도가 그 좌표로 화면 범위를 잡지 않게)")
    void unverifiedCoordinatesAreNotStored() throws Exception {
        service.saveAiRouteToDb(TRIP, aiRoute());

        JsonNode places = om.readTree(plan.getRouteJson()).get(0).path("places");
        JsonNode fake = null;
        for (JsonNode p : places) if ("성산 바다뷰 비밀카페".equals(p.path("name").asText())) fake = p;
        assertThat(fake).isNotNull();
        assertThat(fake.path("isFound").asBoolean()).isFalse();
        assertThat(fake.has("lat") || fake.has("lng")).as("확인 못 한 좌표는 지운다").isFalse();
    }

    @Test
    @DisplayName("정상: 사용자가 직접 요청한 장소는 카카오에서 못 찾아도 남긴다")
    void userRequestedPlaceIsKept() throws Exception {
        service.postProcessRoute(TRIP, aiRoute(), Set.of("성산 바다뷰 비밀카페"), "보통");

        assertThat(savedNames()).containsExactly("성산일출봉", "성산 바다뷰 비밀카페");
    }

    @Test
    @DisplayName("정상: 카카오에서 찾은 장소는 카카오 좌표로 저장된다")
    void verifiedPlaceKeepsKakaoCoordinates() throws Exception {
        service.saveAiRouteToDb(TRIP, aiRoute());

        JsonNode real = om.readTree(plan.getRouteJson()).get(0).path("places").get(0);
        assertThat(real.path("isFound").asBoolean()).isTrue();
        assertThat(real.path("lat").asDouble()).isEqualTo(33.4590);
        assertThat(real.path("lng").asDouble()).isEqualTo(126.9425);
    }
}
