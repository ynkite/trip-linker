package idusw.sbb.triplinker.domain.route.controller;

import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.domain.plan.service.TripAccessGuard;
import idusw.sbb.triplinker.domain.plan.service.TripReadGuard;
import idusw.sbb.triplinker.domain.route.service.AiRouteService;
import idusw.sbb.triplinker.domain.route.service.DemoRouteService;
import idusw.sbb.triplinker.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips/{tripId}/routes")
@RequiredArgsConstructor
public class AiRouteController {

    private final AiRouteService aiRouteService;
    private final DemoRouteService demoRouteService;
    // 일정을 바꾸거나 AI 를 부르는 주소는 그 여행의 주인 · 편집자만 (조회 GET 은 공유 링크라 그대로 공개)
    private final TripAccessGuard tripAccessGuard;
    private final TripReadGuard tripReadGuard;

    private void requireEditor(Long tripId, CustomUserDetails user) {
        tripAccessGuard.requireEditor(tripId, user != null ? user.getUserId() : null);
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Object>> generateRoute(@AuthenticationPrincipal CustomUserDetails user,
                                                             @PathVariable Long tripId) {
        requireEditor(tripId, user);
        // ★고정 경로 모드: AI를 한 번도 호출하지 않고 저장해 둔 경로를 그대로 돌려준다.
        //   토큰 비용이 0이 되며, 끄려면 APP_DEMO_ROUTE_ENABLED=false 로 재시작한다.
        if (demoRouteService.isEnabled()) {
            java.util.Map<String, Object> demo = new java.util.HashMap<>();
            demo.put("route", demoRouteService.applyTo(tripId));
            demo.put("over50", java.util.List.of());
            demo.put("needConfirm", false);
            demo.put("demo", true);
            return ResponseEntity.ok(ApiResponse.success(demo));
        }

        // 1) AI가 type별 장소 후보(이름+sub+stars)만 생성 — 좌표·순서·날짜 없음
        String candidatesJson = aiRouteService.generateCandidates(tripId);

        // 2) 후보 → 좌표 확보 → 일자별 클러스터링 → 시간 골격 조립
        String routeJson = aiRouteService.assembleCandidates(tripId, candidatesJson);

        // 3) DB 저장 (카카오 transit 보정 + 당일치기 숙소 차단 포함)
        aiRouteService.saveAiRouteToDb(tripId, routeJson);

        // 4) 최종 정리(코드 기반): 한 방향 정렬 + 먼 장소/밀도초과 삭제 (AI 생성·추가 없음)
        //    - 사용자요청인데 숙소에서 먼 장소는 삭제하지 않고 over50으로 반환 → 프론트 알림
        java.util.List<String> over50 = aiRouteService.finalizeRoute(tripId);

        // 최종본을 다시 읽어서 반환
        Object finalRoute = aiRouteService.getRoutesByTripId(tripId);

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("route", finalRoute);
        data.put("over50", over50);
        data.put("needConfirm", !over50.isEmpty());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // 컨트롤러에 추가해야 할 GET 매핑 (데이터 반환용)
    @GetMapping("")
    public ResponseEntity<ApiResponse<Object>> getRoutes(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long tripId,
            @RequestHeader(value = TripReadGuard.HEADER, required = false) String shareToken) {
        // 공유 링크로 로그인 없이 열 수 있게 열어 둔 주소 — 번호만 바꿔 남의 일정을 읽지 못하게 토큰을 본다
        tripReadGuard.requireReader(tripId, user != null ? user.getUserId() : null, shareToken);
        // routeService -> aiRouteService 로 수정
        return ResponseEntity.ok(ApiResponse.success(aiRouteService.getRoutesByTripId(tripId)));
    }

    @PostMapping("/replace")
    public ResponseEntity<?> replaceRoutePlaces(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long tripId,
            @RequestBody java.util.Map<String, java.util.List<java.util.Map<String, String>>> payload) {
        requireEditor(tripId, user);

        // ★고정 경로 모드에서는 교체가 AI 호출이므로 막는다.
        if (demoRouteService.isEnabled()) {
            return ResponseEntity.ok(
                    java.util.Map.of("success", false, "message", DemoRouteService.BLOCKED_MESSAGE));
        }

        java.util.List<java.util.Map<String, String>> requests = payload.get("requests");
        // 위에서 만든 서비스 메서드 호출
        String updatedRouteJson = aiRouteService.replaceAiRoutePlaces(tripId, requests);
        // 새로 받아온 JSON을 DB에 덮어쓰고 반환
        aiRouteService.saveAiRouteToDb(tripId, updatedRouteJson);

        return ResponseEntity.ok(
                java.util.Map.of("success", true, "data", updatedRouteJson)
        );
    }

    // 날씨악화 실내 일정 교체 API
    @PostMapping("/indoor-replace")
    public ResponseEntity<?> replaceDayIndoor(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long tripId,
            @RequestParam int day) {
        requireEditor(tripId, user);

        // ★고정 경로 모드에서는 실내 일정 교체도 AI 호출이므로 막는다.
        if (demoRouteService.isEnabled()) {
            return ResponseEntity.ok(
                    java.util.Map.of("success", false, "message", DemoRouteService.BLOCKED_MESSAGE));
        }

        // AI한테 실내 일정으로 교체하라고 시킴
        String newRouteJson = aiRouteService.replaceDayWithIndoor(tripId, day);

        // 바뀐 일정을 DB에 즉시 덮어쓰기 저장
        aiRouteService.saveAiRouteToDb(tripId, newRouteJson);

        // 성공 시 프론트엔드에 새 JSON 내려줌
        return ResponseEntity.ok(
                java.util.Map.of("success", true, "data", newRouteJson)
        );
    }

    @PostMapping("/reorder")
    public ResponseEntity<?> updateRouteManually(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long tripId,
            @RequestBody com.fasterxml.jackson.databind.JsonNode routeData) {
        requireEditor(tripId, user);
        try {
            String json = routeData.toString();
            // 저장(내부에서 카카오 거리/시간/비용 재계산 + budget 갱신 수행)
            aiRouteService.saveAiRouteToDb(tripId, json);
            // 보정된 JSON을 돌려줘서 프론트가 새로고침 없이 화면을 다시 그릴 수 있게 한다
            Object updated = aiRouteService.getRoutesByTripId(tripId);
            return ResponseEntity.ok(java.util.Map.of("success", true, "data", updated));
        } catch (Exception e) {
            // 내부 예외 문구는 서버 로그에만 남기고 응답에는 일반 안내만 준다
            org.slf4j.LoggerFactory.getLogger(AiRouteController.class).warn("일정 저장 실패 tripId={}", tripId, e);
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", "일정을 저장하지 못했습니다. 잠시 후 다시 시도해 주세요."));
        }
    }


}