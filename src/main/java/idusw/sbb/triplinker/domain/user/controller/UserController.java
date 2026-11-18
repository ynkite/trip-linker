package idusw.sbb.triplinker.domain.user.controller;

import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.domain.post.dto.PostListResponseDto;
import idusw.sbb.triplinker.domain.post.service.PostService;
import idusw.sbb.triplinker.domain.user.dto.UserNicknameUpdateRequest;
import idusw.sbb.triplinker.domain.user.dto.UserInfoResponseDto;
import idusw.sbb.triplinker.domain.user.service.UserService;
import idusw.sbb.triplinker.global.common.ApiResponse;
import idusw.sbb.triplinker.global.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

//  회원 정보 관리(조회, 수정, 탈퇴)를 위한 REST Controller
//  클라이언트의 요청을 받아 유저 서비스 로직(UserService)으로 연결하는 진입점의 역할

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    // 내 프로필 조회 (GET) - JWT에서 추출한 현재 로그인 유저 정보를 반환합니다.
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponseDto response = userService.getProfile(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("조회 성공", response));
    }

    // 회원 프로필 조회 (GET) - 특정 유저의 상세 정보를 반환합니다.
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponseDto> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @PathVariable Long userId) {
        // 이메일 · 생년월일이 들어 있어 본인과 관리자만 (화면은 /me 를 쓰고 이 주소는 부르지 않음)
        requireSelfOrAdmin(userDetails, userId);
        UserInfoResponseDto response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    // 닉네임 변경 (PATCH) - 유저의 name(닉네임) 필드를 수정합니다
    @PatchMapping("/{userId}/nickname")
    public ResponseEntity<String> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            @RequestBody UserNicknameUpdateRequest request) {
        // 전에는 주소의 번호만 바꾸면 남의 닉네임을 바꿀 수 있었다
        requireSelfOrAdmin(userDetails, userId);
        userService.updateNickname(userId, request);
        return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
    }

    // 회원 탈퇴 (DELETE /api/users/me) - 개인정보 마스킹 + 토큰 즉시 삭제
    // 본문: 일반 계정 {"password": ...}, 소셜 계정 {"confirmText": "탈퇴하겠습니다"} — 서버가 본인 확인
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdrawMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) Map<String, String> body) {
        Map<String, String> b = body == null ? Map.of() : body;
        userService.withdraw(userDetails.getUserId(), b.get("password"), b.get("confirmText"));
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다.", null));
    }

    // 현재 비밀번호 검증 (POST /api/users/me/verify-password)
    @PostMapping("/me/verify-password")
    public ResponseEntity<ApiResponse<Void>> verifyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {
        boolean ok = userService.verifyPassword(userDetails.getUserId(), body.get("password"));
        if (!ok) return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호가 올바르지 않습니다."));
        return ResponseEntity.ok(ApiResponse.success("확인되었습니다.", null));
    }

    // 내 프로필 수정 (PATCH /api/users/me)
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {
        String name      = body.get("name");
        String region    = body.get("region");
        String gender    = body.get("gender");
        String mbti      = body.get("mbti");
        String birthStr  = body.get("birthDate");
        LocalDate birthDate = (birthStr != null && !birthStr.isBlank()) ? LocalDate.parse(birthStr) : null;

        userService.updateProfile(userDetails.getUserId(), name, region, gender, birthDate, mbti);
        return ResponseEntity.ok(ApiResponse.success("수정되었습니다.", null));
    }

    // 비밀번호 변경 (PATCH /api/users/me/password)
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updateMyPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {
        try {
            userService.updatePassword(
                    userDetails.getUserId(),
                    body.get("currentPassword"),
                    body.get("newPassword"));
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 작성한 후기 목록 조회
    @GetMapping("/me/posts")
    public ResponseEntity<ApiResponse<List<PostListResponseDto>>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.success("작성한 후기 조회 성공", List.of()));
        }
        Long myUserId = Long.valueOf(userDetails.getName());
        List<PostListResponseDto> posts = postService.getMyPosts(myUserId);
        return ResponseEntity.ok(ApiResponse.success("작성한 후기 조회 성공", posts));
    }

    // 좋아요한 후기 목록 조회
    @GetMapping("/me/liked-posts")
    public ResponseEntity<ApiResponse<List<PostListResponseDto>>> getMyLikedPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.success("좋아요한 후기 조회 성공", List.of()));
        }
        Long myUserId = Long.valueOf(userDetails.getName());
        List<PostListResponseDto> likedPosts = postService.getMyLikedPosts(myUserId);
        return ResponseEntity.ok(ApiResponse.success("좋아요한 후기 조회 성공", likedPosts));
    }

    private static void requireSelfOrAdmin(CustomUserDetails me, Long userId) {
        boolean admin = me != null && me.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (me == null || (!admin && !me.getUserId().equals(userId))) {
            throw new ForbiddenException();
        }
    }
}
