package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.domain.auth.entity.OAuthAccount;
import idusw.sbb.triplinker.domain.auth.repository.OAuthAccountRepository;
import idusw.sbb.triplinker.domain.auth.repository.RefreshTokenRepository;
import idusw.sbb.triplinker.domain.expense.repository.ExpenseRepository;
import idusw.sbb.triplinker.domain.place.repository.PlaceRepository;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import idusw.sbb.triplinker.domain.post.repository.PlaceReviewRepository;
import idusw.sbb.triplinker.domain.user.controller.UserController;
import idusw.sbb.triplinker.domain.user.entity.User;
import idusw.sbb.triplinker.domain.user.repository.ScrapRepository;
import idusw.sbb.triplinker.domain.user.repository.UserRepository;
import idusw.sbb.triplinker.domain.user.service.UserServiceImpl;
import idusw.sbb.triplinker.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static idusw.sbb.triplinker.security.TripFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KISA 2-9 약한 비밀번호 정책(BF) · 2-10 불충분한 인증 절차(IA) 재현 시험.
 * - 비밀번호 변경 API 가 새 비밀번호의 규칙을 서버에서 확인하는가 (가입 · 재설정에는 있고 변경에는 없었다)
 * - 탈퇴 API 가 본인 확인을 서버에서 하는가 (화면은 비밀번호를 먼저 확인하지만 탈퇴 요청 자체는 토큰만 봤다)
 */
class AccountChangeTest {

    static final String CURRENT = "Current12!";

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    UserRepository userRepo;
    OAuthAccountRepository oauthRepo;
    User owner;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        oauthRepo = mock(OAuthAccountRepository.class);
        owner = user(OWNER, "owner", "owner@test.local");
        owner.updatePassword(encoder.encode(CURRENT));
        when(userRepo.findById(OWNER)).thenReturn(Optional.of(owner));
        when(oauthRepo.findByUserId(OWNER)).thenReturn(Optional.empty());

        UserServiceImpl service = new UserServiceImpl(userRepo, encoder, oauthRepo,
                mock(RefreshTokenRepository.class), mock(TravelPlanRepository.class), mock(ExpenseRepository.class),
                mock(ScrapRepository.class), mock(PlaceRepository.class), mock(PlaceReviewRepository.class));
        mvc = MockMvcBuilders.standaloneSetup(new UserController(service, null))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        loginAs(OWNER);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("비밀번호 변경: 규칙(8~16자, 영문 · 숫자 · 특수문자)에 안 맞는 새 비밀번호는 서버가 거절한다")
    void weakNewPasswordIsRejected() throws Exception {
        mvc.perform(patch("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + CURRENT + "\",\"newPassword\":\"1234\"}"))
                .andExpect(status().isBadRequest());
        assertThat(encoder.matches(CURRENT, owner.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("정상: 규칙에 맞는 새 비밀번호로는 바뀐다")
    void strongNewPasswordIsAccepted() throws Exception {
        mvc.perform(patch("/api/users/me/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + CURRENT + "\",\"newPassword\":\"NewPass12!\"}"))
                .andExpect(status().isOk());
        assertThat(encoder.matches("NewPass12!", owner.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("탈퇴: 토큰만으로는 탈퇴되지 않는다(비밀번호 없이 보낸 탈퇴 요청)")
    void withdrawWithoutPasswordIsRejected() throws Exception {
        mvc.perform(delete("/api/users/me")).andExpect(status().isBadRequest());
        assertThat(owner.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("탈퇴: 틀린 비밀번호로는 탈퇴되지 않는다")
    void withdrawWithWrongPasswordIsRejected() throws Exception {
        mvc.perform(delete("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Wrong123!\"}"))
                .andExpect(status().isBadRequest());
        assertThat(owner.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("정상: 맞는 비밀번호를 함께 보내면 탈퇴된다")
    void withdrawWithPassword() throws Exception {
        mvc.perform(delete("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + CURRENT + "\"}"))
                .andExpect(status().isOk());
        assertThat(owner.getStatus()).isEqualTo("DELETED");
    }

    @Test
    @DisplayName("정상: 소셜 계정은 비밀번호 대신 확인 문구 「탈퇴하겠습니다」 로 탈퇴된다")
    void socialWithdrawWithConfirmText() throws Exception {
        OAuthAccount social = mock(OAuthAccount.class);
        when(social.getProvider()).thenReturn("test");
        when(oauthRepo.findByUserId(OWNER)).thenReturn(Optional.of(social));

        mvc.perform(delete("/api/users/me")).andExpect(status().isBadRequest());
        assertThat(owner.getStatus()).isEqualTo("ACTIVE");

        mvc.perform(delete("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmText\":\"탈퇴하겠습니다\"}"))
                .andExpect(status().isOk());
        assertThat(owner.getStatus()).isEqualTo("DELETED");
    }
}
