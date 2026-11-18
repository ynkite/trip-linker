package idusw.sbb.triplinker.domain.demo.controller;

import idusw.sbb.triplinker.domain.auth.dto.TokenResponseDto;
import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import idusw.sbb.triplinker.domain.demo.service.DemoLoginService;
import idusw.sbb.triplinker.domain.demo.service.DemoUsageLimiter;
import idusw.sbb.triplinker.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 로그인 화면의 「유저 테스트 로그인」「관리자 테스트 로그인」 단추가 부르는 곳.
 *
 * 요청에 비밀번호를 싣지 않는다. 어느 계정으로 들어갈지는 서버가 정한다.
 * 꺼져 있으면(APP_DEMO_LOGIN_ENABLED=false) 두 주소 모두 404.
 */
@RestController
@RequestMapping("/api/auth/demo")
@RequiredArgsConstructor
public class DemoLoginController {

    private final DemoLoginProperties properties;
    private final DemoLoginService demoLoginService;
    private final DemoUsageLimiter limiter;

    /** 단추를 보여 줄지 화면이 묻는다. */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> status() {
        if (!properties.isEnabled()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("enabled", true)));
    }

    /** body 예: {"role":"user"} 또는 {"role":"admin"} */
    @PostMapping
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(@RequestBody(required = false) Map<String, String> body,
                                                               HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return ResponseEntity.notFound().build();
        }
        String role = body == null ? null : body.get("role");
        DemoLoginService.Kind kind;
        if ("user".equalsIgnoreCase(role)) {
            kind = DemoLoginService.Kind.USER;
        } else if ("admin".equalsIgnoreCase(role)) {
            kind = DemoLoginService.Kind.ADMIN;
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("role 은 user 또는 admin 이어야 합니다."));
        }
        if (!limiter.tryLogin(resolveClientIp(request))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("테스트 로그인 요청이 너무 많습니다. 1분 뒤에 다시 눌러 주세요."));
        }
        return ResponseEntity.ok(ApiResponse.success("로그인 성공", demoLoginService.login(kind)));
    }

    // AuthController 와 같은 방식(프록시 뒤면 X-Forwarded-For 첫 값)
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
