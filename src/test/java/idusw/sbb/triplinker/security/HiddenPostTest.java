package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.domain.place.controller.PlaceController;
import idusw.sbb.triplinker.domain.place.entity.Place;
import idusw.sbb.triplinker.domain.place.entity.PlaceCategory;
import idusw.sbb.triplinker.domain.place.service.PlaceService;
import idusw.sbb.triplinker.domain.post.controller.PostController;
import idusw.sbb.triplinker.domain.post.entity.PlaceReview;
import idusw.sbb.triplinker.domain.post.entity.Post;
import idusw.sbb.triplinker.domain.post.entity.PostScrap;
import idusw.sbb.triplinker.domain.post.repository.*;
import idusw.sbb.triplinker.domain.post.service.PostService;
import idusw.sbb.triplinker.domain.plan.repository.TravelPlanRepository;
import idusw.sbb.triplinker.domain.system.service.SystemService;
import idusw.sbb.triplinker.domain.user.entity.User;
import idusw.sbb.triplinker.domain.user.repository.UserRepository;
import idusw.sbb.triplinker.global.exception.GlobalExceptionHandler;
import idusw.sbb.triplinker.global.service.LocalFileService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 관리자가 숨긴(HIDDEN) 글이 메인 「인기 경로 TOP 3」 에 계속 떠 있던 문제의 재현 시험.
 * 숨김 글은 작성자와 관리자 말고는 목록 · 랭킹 · 상세 주소 · 댓글 · 장소 후기 어느 길로도 보이면 안 된다.
 * 메모리 DB(H2)에 글을 실제로 넣고, 진짜 PostService · 저장소 쿼리를 HTTP 요청으로 부른다.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:hidden_post;MODE=MariaDB;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HiddenPostTest {

    @Autowired EntityManager em;
    @Autowired PostRepository postRepository;
    @Autowired PostLikeRepository postLikeRepository;
    @Autowired PostCommentRepository postCommentRepository;
    @Autowired PostScrapRepository postScrapRepository;
    @Autowired PostImageRepository postImageRepository;
    @Autowired PlaceReviewRepository placeReviewRepository;
    @Autowired UserRepository userRepository;
    @Autowired TravelPlanRepository travelPlanRepository;

    MockMvc mvc;
    User spammer, reader, admin;
    Post spam, normal;
    Place place;

    @BeforeEach
    void setUp() {
        spammer = user("spammer", "USER");
        reader = user("reader", "USER");
        admin = user("admin", "ADMIN");

        spam = post(spammer, "스팸 광고 - 최저가 여행 상품 안내!!!", "HIDDEN");
        normal = post(reader, "부산 1박 2일 바다 코스", "ACTIVE");
        // 숨김 글이 스크랩을 더 많이 받아 랭킹 1위가 되는 상황 (운영 화면과 같은 꼴)
        scrap(reader, spam);
        scrap(admin, spam);
        scrap(spammer, normal);

        place = Place.builder().name("해운대").category(PlaceCategory.ATTRACTION).build();
        em.persist(place);
        em.persist(PlaceReview.builder().place(place).post(spam).user(spammer).rating(5).comment("광고").build());
        em.persist(PlaceReview.builder().place(place).post(normal).user(reader).rating(4).comment("좋아요").build());
        em.flush();

        PostService service = new PostService(postRepository, postLikeRepository, postCommentRepository,
                postScrapRepository, postImageRepository, placeReviewRepository, mock(PlaceService.class),
                userRepository, travelPlanRepository, mock(LocalFileService.class));
        mvc = MockMvcBuilders.standaloneSetup(
                        new PostController(service, mock(SystemService.class), mock(LocalFileService.class)),
                        new PlaceController(placeReviewRepository))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User user(String name, String role) {
        User u = User.builder().username(name).passwordHash("x").name(name)
                .email(name + "@test.local").region("서울").role(role).status("ACTIVE").build();
        em.persist(u);
        return u;
    }

    private Post post(User writer, String title, String status) {
        Post p = Post.builder().user(writer).title(title).content(title + " 본문").category("ROUTE")
                .status(status).isPublic(true).build();
        em.persist(p);
        return p;
    }

    private void scrap(User who, Post p) {
        em.persist(PostScrap.builder().user(who).post(p).category("ROUTE").build());
    }

    private void loginAs(User u) {
        CustomUserDetails d = new CustomUserDetails(u.getId(), u.getRole());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(d, null, d.getAuthorities()));
    }

    @Test
    @DisplayName("숨김 글은 메인 인기 경로 TOP 3(스크랩순)에 나오지 않는다 — 로그인 안 한 사람")
    void hiddenPostNotInTop3ForGuest() throws Exception {
        mvc.perform(get("/api/posts?sort=scrap&size=3&category=ROUTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].title", not(hasItem(spam.getTitle()))))
                .andExpect(jsonPath("$.data.content[0].title").value(normal.getTitle()));
    }

    @Test
    @DisplayName("숨김 글은 다른 회원의 최신순 목록에도 나오지 않는다")
    void hiddenPostNotInListForOtherUser() throws Exception {
        loginAs(reader);
        mvc.perform(get("/api/posts?category=route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].title", not(hasItem(spam.getTitle()))));
    }

    @Test
    @DisplayName("숨김 글 상세 주소를 남이 직접 열면 본문을 받지 못한다")
    void otherCannotOpenHiddenPostDetail() throws Exception {
        loginAs(reader);
        mvc.perform(get("/api/posts/" + spam.getId()))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(not(containsString("본문"))));
        mvc.perform(get("/api/posts/" + spam.getId()).with(r -> { SecurityContextHolder.clearContext(); return r; }))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("숨김 글의 댓글 · 장소 후기, 장소 화면의 후기 목록으로도 보이지 않는다")
    void hiddenPostNotLeakedThroughSideRoutes() throws Exception {
        mvc.perform(get("/api/posts/" + spam.getId() + "/comments")).andExpect(status().is4xxClientError());
        mvc.perform(get("/api/posts/" + spam.getId() + "/place-reviews")).andExpect(status().is4xxClientError());
        mvc.perform(get("/api/places/" + place.getId() + "/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].title", not(hasItem(spam.getTitle()))))
                .andExpect(jsonPath("$.data[*].title", hasItem(normal.getTitle())));
    }

    @Test
    @DisplayName("정상: 작성자는 자기 숨김 글을 목록과 상세에서 본다(숨김 안내를 보려고)")
    void writerStillSeesOwnHiddenPost() throws Exception {
        loginAs(spammer);
        mvc.perform(get("/api/posts?category=route"))
                .andExpect(jsonPath("$.data.content[*].title", hasItem(spam.getTitle())));
        mvc.perform(get("/api/posts/" + spam.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("HIDDEN"));
    }

    @Test
    @DisplayName("정상: 관리자는 숨김 글을 목록 · 상세에서 본다(관리용), 공개 글은 누구나 본다")
    void adminSeesHiddenAndEveryoneSeesActive() throws Exception {
        loginAs(admin);
        mvc.perform(get("/api/posts?sort=scrap&category=route"))
                .andExpect(jsonPath("$.data.content[*].title", hasItem(spam.getTitle())))
                .andExpect(jsonPath("$.data.content[?(@.title == '" + spam.getTitle() + "')].hidden").value(true));
        mvc.perform(get("/api/posts/" + spam.getId())).andExpect(status().isOk());

        SecurityContextHolder.clearContext();
        mvc.perform(get("/api/posts/" + normal.getId())).andExpect(status().isOk());
        mvc.perform(get("/api/posts/" + normal.getId() + "/comments")).andExpect(status().isOk());
    }
}
