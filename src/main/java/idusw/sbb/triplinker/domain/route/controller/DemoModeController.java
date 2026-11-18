package idusw.sbb.triplinker.domain.route.controller;

import idusw.sbb.triplinker.domain.route.service.DemoRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 프론트가 고정 경로 모드 여부를 알아내는 용도.
 * 켜져 있으면 상단 안내 바를 띄우고, 교체 버튼을 눌렀을 때 바로 안내 문구를 보여준다.
 */
@RestController
@RequiredArgsConstructor
public class DemoModeController {

    private final DemoRouteService demoRouteService;

    @GetMapping("/api/demo-mode")
    public ResponseEntity<Map<String, Object>> demoMode() {
        boolean on = demoRouteService.isEnabled();
        return ResponseEntity.ok(Map.of(
                "enabled", on,
                "notice", DemoRouteService.NOTICE_MESSAGE,
                "blocked", DemoRouteService.BLOCKED_MESSAGE
        ));
    }
}
