package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.domain.budget.controller.BudgetEstimateController;
import idusw.sbb.triplinker.domain.budget.service.BudgetEstimateService;
import idusw.sbb.triplinker.domain.chat.controller.ChatbotController;
import idusw.sbb.triplinker.domain.chat.entity.ChatSession;
import idusw.sbb.triplinker.domain.chat.repository.ChatMessageRepository;
import idusw.sbb.triplinker.domain.chat.repository.ChatSessionRepository;
import idusw.sbb.triplinker.domain.chat.service.ChatbotServiceImpl;
import idusw.sbb.triplinker.domain.expense.controller.ExpenseController;
import idusw.sbb.triplinker.domain.expense.repository.ExpenseRepository;
import idusw.sbb.triplinker.domain.expense.service.ExpenseService;
import idusw.sbb.triplinker.domain.plan.controller.TravelPlanController;
import idusw.sbb.triplinker.domain.plan.repository.PlanInputFormRepository;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import idusw.sbb.triplinker.domain.plan.service.TravelPlanServiceImpl;
import idusw.sbb.triplinker.domain.plan.service.TripAccessGuard;
import idusw.sbb.triplinker.domain.plan.service.TripReadGuard;
import idusw.sbb.triplinker.domain.planshare.service.ShareTokenService;
import idusw.sbb.triplinker.domain.post.entity.Post;
import idusw.sbb.triplinker.domain.post.repository.PostRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import idusw.sbb.triplinker.domain.planshare.controller.TripShareController;
import idusw.sbb.triplinker.domain.planshare.entity.PlanRole;
import idusw.sbb.triplinker.domain.planshare.repository.TripMemberRepository;
import idusw.sbb.triplinker.domain.planshare.service.TripShareService;
import idusw.sbb.triplinker.domain.route.controller.AiRouteController;
import idusw.sbb.triplinker.domain.route.service.AiRouteService;
import idusw.sbb.triplinker.domain.route.service.DemoRouteService;
import idusw.sbb.triplinker.domain.user.controller.UserController;
import idusw.sbb.triplinker.domain.user.dto.UserInfoResponseDto;
import idusw.sbb.triplinker.domain.user.repository.UserRepository;
import idusw.sbb.triplinker.domain.user.service.UserService;
import idusw.sbb.triplinker.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static idusw.sbb.triplinker.security.TripFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OWASP A01(접근 제어 실패) 재현 시험.
 * 「다른 회원의 여행 번호 · 지출 번호만 바꿔 보내면 읽거나 고칠 수 있는가」 를 HTTP 요청 단위로 확인한다.
 * 같은 묶음에 정상 흐름(주인 · 초대된 편집자 · 로그인 없이 공유 링크 읽기)도 넣어, 막다가 정상 사용이 깨지지 않았는지 같이 본다.
 */
class AccessControlTest {

    TripFixture fx;
    TravelPlanRepository planRepo;
    TripMemberRepository memberRepo;
    ExpenseRepository expenseRepo;
    PlanInputFormRepository formRepo;
    UserRepository userRepo;
    AiRouteService aiRouteService;
    DemoRouteService demoRouteService;
    ChatSessionRepository sessionRepo;
    ChatMessageRepository messageRepo;
    ChatModel claude, groq, gemini;
    UserService userService;
    BudgetEstimateService budgetEstimateService;
    ShareTokenService shareTokens;
    PostRepository postRepo;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        fx = new TripFixture();
        planRepo = mock(TravelPlanRepository.class);
        memberRepo = mock(TripMemberRepository.class);
        expenseRepo = mock(ExpenseRepository.class);
        formRepo = mock(PlanInputFormRepository.class);
        userRepo = mock(UserRepository.class);
        aiRouteService = mock(AiRouteService.class);
        demoRouteService = mock(DemoRouteService.class);
        sessionRepo = mock(ChatSessionRepository.class);
        messageRepo = mock(ChatMessageRepository.class);
        claude = mock(ChatModel.class);
        groq = mock(ChatModel.class);
        gemini = mock(ChatModel.class);
        userService = mock(UserService.class);
        budgetEstimateService = mock(BudgetEstimateService.class);

        when(planRepo.findById(TRIP)).thenReturn(Optional.of(fx.plan));
        when(planRepo.findById(OTHER_TRIP)).thenReturn(Optional.of(fx.otherPlan));
        when(expenseRepo.findById(EXPENSE)).thenReturn(Optional.of(fx.expense));
        when(expenseRepo.findByPlanId(TRIP)).thenReturn(List.of(fx.expense));
        when(userRepo.findById(OWNER)).thenReturn(Optional.of(fx.owner));
        when(userRepo.findById(OTHER)).thenReturn(Optional.of(fx.other));
        when(userRepo.findById(EDITOR)).thenReturn(Optional.of(fx.editor));
        when(userRepo.findByEmail("editor@test.local")).thenReturn(Optional.of(fx.editor));
        when(userRepo.findByEmail("other@test.local")).thenReturn(Optional.of(fx.other));
        when(memberRepo.findByTravelPlanId(TRIP)).thenReturn(List.of(fx.editorMember));
        when(memberRepo.existsByTravelPlanIdAndUserIdAndRole(TRIP, EDITOR, PlanRole.EDITOR)).thenReturn(true);
        when(memberRepo.existsByTravelPlanIdAndUserId(TRIP, EDITOR)).thenReturn(true);
        when(aiRouteService.getRoutesByTripId(anyLong())).thenReturn(List.of());
        when(aiRouteService.finalizeRoute(anyLong())).thenReturn(List.of());
        when(userService.getProfile(anyLong())).thenAnswer(inv ->
                new UserInfoResponseDto(inv.getArgument(0, Long.class) == OWNER ? fx.owner : fx.other, false));

        ChatSession victimSession = new ChatSession();
        victimSession.setId(77L);
        victimSession.setPlanId(TRIP);
        when(sessionRepo.findById(77L)).thenReturn(Optional.of(victimSession));
        when(sessionRepo.save(any())).thenAnswer(inv -> {
            ChatSession s = inv.getArgument(0);
            s.setId(78L);
            return s;
        });

        TripAccessGuard guard = new TripAccessGuard(planRepo, memberRepo);
        shareTokens = new ShareTokenService("test-secret");
        postRepo = mock(PostRepository.class);
        when(postRepo.findByPlanIdAndStatusOrderByCreatedAtDesc(anyLong(), anyString(), any())).thenReturn(Page.empty());
        TripReadGuard readGuard = new TripReadGuard(planRepo, memberRepo, shareTokens, postRepo);
        mvc = MockMvcBuilders.standaloneSetup(
                        new ExpenseController(new ExpenseService(expenseRepo, planRepo, guard)),
                        new AiRouteController(aiRouteService, demoRouteService, guard, readGuard),
                        new TripShareController(new TripShareService(memberRepo, planRepo, userRepo, mock(JavaMailSender.class), guard, shareTokens)),
                        new TravelPlanController(new TravelPlanServiceImpl(planRepo, formRepo, userRepo, expenseRepo, memberRepo, guard), readGuard),
                        new ChatbotController(new ChatbotServiceImpl(sessionRepo, messageRepo, planRepo, guard, claude, groq, gemini)),
                        new UserController(userService, null),
                        new BudgetEstimateController(budgetEstimateService, guard))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ───────────────────────── 가계부(지출) ─────────────────────────
    @Nested
    @DisplayName("가계부")
    class Expenses {
        @Test
        @DisplayName("다른 회원이 남의 여행 가계부를 읽을 수 없다")
        void otherCannotReadReport() throws Exception {
            loginAs(OTHER);
            mvc.perform(get("/api/trips/10/expenses")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("다른 회원이 남의 지출 금액을 고칠 수 없다")
        void otherCannotUpdate() throws Exception {
            loginAs(OTHER);
            mvc.perform(put("/api/trips/10/expenses/100").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"FOOD\",\"description\":\"x\",\"amount\":1}"))
                    .andExpect(status().isForbidden());
            assertThat(fx.expense.getAmount()).isEqualTo(50_000L);
        }

        @Test
        @DisplayName("자기 여행 번호에 남의 지출 번호를 섞어 보내도 지울 수 없다")
        void otherCannotDeleteViaOwnTrip() throws Exception {
            loginAs(OTHER);
            mvc.perform(delete("/api/trips/20/expenses/100")).andExpect(status().isForbidden());
            verify(expenseRepo, never()).delete(any());
        }

        @Test
        @DisplayName("다른 회원이 남의 가계부에 지출을 넣을 수 없다")
        void otherCannotAdd() throws Exception {
            loginAs(OTHER);
            mvc.perform(post("/api/trips/10/expenses").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"FOOD\",\"description\":\"x\",\"amount\":1}"))
                    .andExpect(status().isForbidden());
            verify(expenseRepo, never()).save(any());
        }

        @Test
        @DisplayName("정상: 주인은 가계부를 읽고 지출을 고치고 지운다")
        void ownerCanUseLedger() throws Exception {
            loginAs(OWNER);
            mvc.perform(get("/api/trips/10/expenses")).andExpect(status().isOk());
            mvc.perform(put("/api/trips/10/expenses/100").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"FOOD\",\"description\":\"x\",\"amount\":7000}"))
                    .andExpect(status().isOk());
            assertThat(fx.expense.getAmount()).isEqualTo(7000L);
            mvc.perform(post("/api/trips/10/expenses").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"FOOD\",\"description\":\"x\",\"amount\":1}"))
                    .andExpect(status().isOk());
            mvc.perform(delete("/api/trips/10/expenses/100")).andExpect(status().isOk());
            verify(expenseRepo).delete(fx.expense);
        }

        @Test
        @DisplayName("정상: 초대된 편집자도 공유 여행의 가계부를 쓴다")
        void editorCanUseLedger() throws Exception {
            loginAs(EDITOR);
            mvc.perform(get("/api/trips/10/expenses")).andExpect(status().isOk());
            mvc.perform(post("/api/trips/10/expenses").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"category\":\"FOOD\",\"description\":\"x\",\"amount\":1}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("다른 회원은 남의 여행 예산 근거(목적지 · 예산)도 볼 수 없다")
        void otherCannotReadBudgetEstimate() throws Exception {
            loginAs(OTHER);
            mvc.perform(get("/api/trips/10/budget-estimate")).andExpect(status().isForbidden());
            verify(budgetEstimateService, never()).estimate(anyLong());
            loginAs(OWNER);
            mvc.perform(get("/api/trips/10/budget-estimate")).andExpect(status().isOk());
        }
    }

    // ───────────────────────── 일정(경로) ─────────────────────────
    @Nested
    @DisplayName("일정 경로")
    class Routes {
        @Test
        @DisplayName("다른 회원이 남의 일정을 순서 바꾸기로 덮어쓸 수 없다")
        void otherCannotReorder() throws Exception {
            loginAs(OTHER);
            mvc.perform(post("/api/trips/10/routes/reorder").contentType(MediaType.APPLICATION_JSON)
                            .content("[{\"day\":1,\"places\":[{\"name\":\"<img src=x onerror=alert(1)>\"}]}]"))
                    .andExpect(status().isForbidden());
            verify(aiRouteService, never()).saveAiRouteToDb(anyLong(), anyString());
        }

        @Test
        @DisplayName("다른 회원이 남의 여행으로 AI 일정 생성 · 장소 교체 · 실내 교체를 부를 수 없다 (요금도 막힘)")
        void otherCannotCallAi() throws Exception {
            loginAs(OTHER);
            mvc.perform(post("/api/trips/10/routes/generate")).andExpect(status().isForbidden());
            mvc.perform(post("/api/trips/10/routes/replace").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"requests\":[]}")).andExpect(status().isForbidden());
            mvc.perform(post("/api/trips/10/routes/indoor-replace?day=1")).andExpect(status().isForbidden());
            verify(aiRouteService, never()).assembleCandidates(anyLong(), any());
            verify(aiRouteService, never()).replaceAiRoutePlaces(anyLong(), any());
            verify(aiRouteService, never()).replaceDayWithIndoor(anyLong(), anyInt());
        }

        @Test
        @DisplayName("정상: 주인 · 편집자는 순서를 바꿔 저장한다")
        void ownerAndEditorCanReorder() throws Exception {
            for (Long who : new Long[]{OWNER, EDITOR}) {
                loginAs(who);
                mvc.perform(post("/api/trips/10/routes/reorder").contentType(MediaType.APPLICATION_JSON)
                        .content("[]")).andExpect(status().isOk());
            }
            verify(aiRouteService, times(2)).saveAiRouteToDb(eq(TRIP), anyString());
        }

        @Test
        @DisplayName("정상: 공유 링크 — 로그인 없이도 일정 · 여행 정보 · 인원 정보를 읽는다")
        void anonymousCanReadSharedTrip() throws Exception {
            loginAs(null);
            String tok = shareTokens.tokenFor(TRIP);   // 주인이 「공유」 를 눌러 받은 링크의 토큰
            mvc.perform(get("/api/trips/10/routes").header("X-Share-Token", tok)).andExpect(status().isOk());
            mvc.perform(get("/api/trips/10").header("X-Share-Token", tok)).andExpect(status().isOk());
            mvc.perform(get("/api/trips/10/input-form").header("X-Share-Token", tok)).andExpect(status().isOk());
        }
    }

    // ───────────────────────── 로그인 없는 읽기 (공유 링크) ─────────────────────────
    // 대표 선택 5번: 경로 조회가 로그인 없이 열려 있어 여행 번호만 바꿔 모든 회원의 일정 · 예산 · 출발지를 읽을 수 있던 것
    @Nested
    class PublicRead {
        @Test
        @DisplayName("로그인 없이 여행 번호만으로는 일정 · 여행 정보 · 인원 정보를 못 읽는다")
        void anonymousWithoutTokenCannotRead() throws Exception {
            loginAs(null);
            mvc.perform(get("/api/trips/10/routes")).andExpect(status().isForbidden());
            mvc.perform(get("/api/trips/10")).andExpect(status().isForbidden());
            mvc.perform(get("/api/trips/10/input-form")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("로그인한 다른 회원도 멤버가 아니면 번호만 바꿔 남의 일정을 못 읽는다")
        void otherUserCannotReadByNumber() throws Exception {
            loginAs(OTHER);
            mvc.perform(get("/api/trips/10/routes")).andExpect(status().isForbidden());
            mvc.perform(get("/api/trips/10")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("예전 공유 토큰(번호 XOR 고정값 — 화면 JS 로 거꾸로 풀림)으로는 못 읽는다")
        void legacyGuessableTokenRejected() throws Exception {
            loginAs(null);
            String legacy = Long.toHexString(TRIP ^ 0x5A3C9B7D2EL);
            mvc.perform(get("/api/trips/10/routes").header("X-Share-Token", legacy)).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("다른 여행의 공유 토큰을 번호만 바꿔 붙여도 못 읽는다")
        void tokenOfOtherTripRejected() throws Exception {
            loginAs(null);
            String forged = TRIP + shareTokens.tokenFor(OTHER_TRIP).substring(String.valueOf(OTHER_TRIP).length());
            mvc.perform(get("/api/trips/10/routes").header("X-Share-Token", forged)).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("정상: 주인 · 초대된 멤버는 토큰 없이 읽는다")
        void ownerAndMemberRead() throws Exception {
            loginAs(OWNER);
            mvc.perform(get("/api/trips/10/routes")).andExpect(status().isOk());
            loginAs(EDITOR);
            mvc.perform(get("/api/trips/10")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("정상: 커뮤니티에 공개된 후기 글의 원본 일정은 누구나 읽는다(후기 화면이 원본 일정을 보여 줌)")
        void sourcePlanOfPublicPostIsReadable() throws Exception {
            when(postRepo.findByPlanIdAndStatusOrderByCreatedAtDesc(eq(TRIP), eq("ACTIVE"), any()))
                    .thenReturn(new PageImpl<>(List.of(mock(Post.class))));
            loginAs(OTHER);
            mvc.perform(get("/api/trips/10")).andExpect(status().isOk());
            loginAs(null);
            mvc.perform(get("/api/trips/10/routes")).andExpect(status().isOk());
        }
    }

    // ───────────────────────── 공유 · 멤버 ─────────────────────────
    @Nested
    @DisplayName("공유 멤버")
    class Members {
        @Test
        @DisplayName("다른 회원이 남의 여행 멤버 이메일 목록을 볼 수 없다")
        void otherCannotListMembers() throws Exception {
            loginAs(OTHER);
            mvc.perform(get("/api/trips/10/members")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("다른 회원이 자기를 남의 여행 편집자로 넣을 수 없다")
        void otherCannotInviteSelf() throws Exception {
            loginAs(OTHER);
            mvc.perform(post("/api/trips/10/members").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"other@test.local\",\"role\":\"EDITOR\"}"))
                    .andExpect(status().isForbidden());
            verify(memberRepo, never()).save(any());
        }

        @Test
        @DisplayName("정상: 주인은 멤버를 보고 초대한다")
        void ownerCanInvite() throws Exception {
            loginAs(OWNER);
            mvc.perform(get("/api/trips/10/members")).andExpect(status().isOk());
            mvc.perform(post("/api/trips/10/members").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"other@test.local\",\"role\":\"EDITOR\"}")).andExpect(status().isOk());
            verify(memberRepo).save(any());
        }
    }

    // ───────────────────────── 여행 조건 ─────────────────────────
    @Nested
    @DisplayName("여행 조건")
    class InputForm {
        @Test
        @DisplayName("다른 회원이 남의 여행 목적지 · 예산을 바꿀 수 없다")
        void otherCannotPatchInputForm() throws Exception {
            loginAs(OTHER);
            mvc.perform(patch("/api/trips/10/input-form").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"destination\":\"부산\",\"budget\":\"1\"}"))
                    .andExpect(status().isForbidden());
            verify(planRepo, never()).updateDestination(anyLong(), anyString());
        }

        @Test
        @DisplayName("다른 회원이 남의 여행에 자기 취향을 덮어 넣을 수 없다")
        void otherCannotLoadPreferenceIntoOthersTrip() throws Exception {
            loginAs(OTHER);
            mvc.perform(post("/api/trips/10/load-preference")).andExpect(status().isForbidden());
            verify(formRepo, never()).save(any());
        }

        @Test
        @DisplayName("정상: 주인은 여행 조건을 바꾼다")
        void ownerCanPatch() throws Exception {
            loginAs(OWNER);
            mvc.perform(patch("/api/trips/10/input-form").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"destination\":\"부산\"}")).andExpect(status().isOk());
            verify(planRepo).updateDestination(TRIP, "부산");
        }
    }

    // ───────────────────────── 챗봇 ─────────────────────────
    @Nested
    @DisplayName("챗봇")
    class Chat {
        @Test
        @DisplayName("다른 회원이 남의 여행으로 챗봇 방을 만들 수 없다 (방 안에 남의 예산 · 취향이 들어감)")
        void otherCannotOpenSessionOnOthersPlan() throws Exception {
            loginAs(OTHER);
            mvc.perform(post("/api/chat/sessions").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"planId\":10}")).andExpect(status().isForbidden());
            verify(sessionRepo, never()).save(any());
        }

        @Test
        @DisplayName("다른 회원이 남의 대화방 번호로 AI 를 부를 수 없다")
        void otherCannotTalkInOthersSession() throws Exception {
            loginAs(OTHER);
            mvc.perform(post("/api/chat/message").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sessionId\":77,\"message\":\"이 여행 예산 알려줘\"}")).andExpect(status().isForbidden());
            verify(claude, never()).call(any(Prompt.class));
            verify(messageRepo, never()).save(any());
        }

        @Test
        @DisplayName("정상: 주인은 자기 여행으로 챗봇 방을 만든다")
        void ownerCanOpenSession() throws Exception {
            loginAs(OWNER);
            mvc.perform(post("/api/chat/sessions").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"planId\":10}")).andExpect(status().isOk());
        }
    }

    // ───────────────────────── 회원 정보 ─────────────────────────
    @Nested
    @DisplayName("회원 정보")
    class Users {
        @Test
        @DisplayName("다른 회원의 닉네임을 바꿀 수 없다")
        void otherCannotRenameSomeoneElse() throws Exception {
            loginAs(OTHER);
            mvc.perform(patch("/api/users/1/nickname").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nickname\":\"바뀜\"}")).andExpect(status().isForbidden());
            verify(userService, never()).updateNickname(eq(OWNER), any());
        }

        @Test
        @DisplayName("다른 회원의 이메일 · 생년월일을 번호로 조회할 수 없다")
        void otherCannotReadSomeoneElsesProfile() throws Exception {
            loginAs(OTHER);
            mvc.perform(get("/api/users/1")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("정상: 자기 정보는 조회한다")
        void selfCanRead() throws Exception {
            loginAs(OWNER);
            mvc.perform(get("/api/users/1")).andExpect(status().isOk());
            mvc.perform(get("/api/users/me")).andExpect(status().isOk());
        }
    }
}
