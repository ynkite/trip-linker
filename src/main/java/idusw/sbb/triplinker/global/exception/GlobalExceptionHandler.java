package idusw.sbb.triplinker.global.exception;

import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SocialAccountExistException.class)
    public ResponseEntity<Map<String, String>> handleSocialAccountExistException(SocialAccountExistException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("code", "SOCIAL_ACCOUNT_EXIST");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("provider", ex.getProvider());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(LoginFailException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleLoginFail(LoginFailException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("locked", ex.isLocked());
        data.put("failCount", ex.getFailCount());
        if (ex.isLocked()) {
            data.put("remainSeconds", ex.getRemainSeconds());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), data));
    }

    // 비밀번호는 맞지만 지금 규칙에 맞지 않을 때: 토큰 없이 재설정 안내만
    @ExceptionHandler(WeakPasswordLoginException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleWeakPassword(WeakPasswordLoginException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("weakPassword", true);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage(), data));
    }

    // 남의 여행 · 지출 · 회원 정보에 손댈 때 (로그인은 했지만 주인 · 멤버가 아님)
    // 번호를 바꿔 가며 남의 것을 열어 보는 시도를 나중에 찾을 수 있게 주소와 회원 번호를 로그에 남긴다(개인정보는 안 남김).
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object user = (auth != null && auth.getPrincipal() instanceof CustomUserDetails u) ? u.getUserId() : "-";
        log.warn("[권한 거부] {} {} user={}", request.getMethod(), request.getRequestURI(), user);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    // 인증 메일 · AI 호출을 짧은 시간에 너무 많이 보낼 때
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooMany(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.error(ex.getMessage()));
    }

    // 사유 누락, 존재하지 않는 ID 조회, 잘못된 상태 변경 시도 등
    // 프로젝트 전반에서 공통으로 던지는 일반 검증 예외를 한 곳에서 처리
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
}