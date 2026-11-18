package idusw.sbb.triplinker.domain.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.expense.entity.Expense;
import idusw.sbb.triplinker.domain.place.entity.Place;
import idusw.sbb.triplinker.domain.place.entity.PlaceCategory;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.post.entity.Post;
import idusw.sbb.triplinker.domain.post.entity.PostComment;
import idusw.sbb.triplinker.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 메모리 DB(H2, MariaDB 호환 모드)에서 되돌리기를 실제로 돌려 본다.
 * 운영 DB 설정 없이도 돌도록 데이터소스를 직접 지정한다.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo_seed;MODE=MariaDB;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.sql.init.mode=never"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DemoSeedService.class, DemoSeedServiceJpaTest.JsonConfig.class})
class DemoSeedServiceJpaTest {

    @TestConfiguration
    static class JsonConfig {
        @Bean
        ObjectMapper objectMapper() { return new ObjectMapper(); }
    }

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 19);

    @Autowired DemoSeedService seedService;
    @Autowired EntityManager em;

    private User demo;
    private User other;
    private Post otherPost;
    private TravelPlan otherPlan;

    @BeforeEach
    void setUp() {
        demo = user("demo_user", "demo_user@example.com", "USER");
        other = user("user01", "minjun@test.com", "USER");

        otherPlan = TravelPlan.builder().user(other).title("남의 일정").destination("부산")
                .startDate(TODAY).endDate(TODAY.plusDays(1)).status("FIXED").build();
        em.persist(otherPlan);
        otherPost = Post.builder().user(other).title("남의 글").content("내용").isPublic(true).build();
        em.persist(otherPost);
        for (int i = 0; i < 3; i++) {
            em.persist(Place.builder().name("장소" + i).category(PlaceCategory.ATTRACTION).build());
        }
        em.flush();
    }

    private User user(String username, String email, String role) {
        User u = User.builder().username(username).passwordHash("x").name(username)
                .email(email).region("서울").role(role).status("ACTIVE").build();
        em.persist(u);
        return u;
    }

    private long count(String jpql) {
        return em.createQuery(jpql, Long.class).setParameter("uid", demo.getId()).getSingleResult();
    }

    @Test
    @DisplayName("되돌리면 일정 4개(지역 4곳), 가계부, 후기 3개, 스크랩이 들어간다")
    void seedsExampleData() throws Exception {
        seedService.reset(demo.getId(), TODAY);

        List<TravelPlan> plans = em.createQuery(
                "SELECT p FROM TravelPlan p WHERE p.user.id = :uid", TravelPlan.class)
                .setParameter("uid", demo.getId()).getResultList();
        assertThat(plans).hasSize(4);
        assertThat(plans).extracting(TravelPlan::getDestination)
                .containsExactlyInAnyOrder("서울", "제주", "경주", "전주");
        assertThat(plans).allMatch(p -> "FIXED".equals(p.getStatus()) && p.getForm() != null);
        // 지난 여행과 다가오는 여행이 섞여 있다
        assertThat(plans).anyMatch(p -> p.getEndDate().isBefore(TODAY));
        assertThat(plans).anyMatch(p -> p.getStartDate().isAfter(TODAY));
        // 만든 시각은 과거로 돌려 둔다(관리자 '오늘 새 일정' 통계에 안 잡히게)
        assertThat(plans).allMatch(p -> p.getCreatedAt().isBefore(TODAY.atStartOfDay()));

        // 지도: 모든 장소에 좌표가 있고 장소 사이에 이동 줄이 있다
        TravelPlan jeju = plans.stream().filter(p -> "제주".equals(p.getDestination())).findFirst().orElseThrow();
        JsonNode route = new ObjectMapper().readTree(jeju.getRouteJson());
        assertThat(route).hasSize(3);
        JsonNode day1 = route.get(0).get("places");
        assertThat(day1.get(0).get("lat").isNumber()).isTrue();
        assertThat(day1.get(0).get("isFound").asBoolean()).isTrue();
        assertThat(day1.get(1).has("transit")).isTrue();

        assertThat(count("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :uid")).isGreaterThanOrEqualTo(20);
        assertThat(count("SELECT COUNT(p) FROM Post p WHERE p.user.id = :uid")).isEqualTo(3);
        assertThat(count("SELECT COUNT(s) FROM PostScrap s WHERE s.user.id = :uid")).isEqualTo(1);
        assertThat(count("SELECT COUNT(s) FROM Scrap s WHERE s.user.id = :uid")).isEqualTo(3);
    }

    @Test
    @DisplayName("테스트 유저가 고치고 지우고 더한 뒤 다시 되돌리면 처음 상태가 되고, 남의 일정·글은 그대로다")
    void resetRestoresSeedAndKeepsOthers() {
        seedService.reset(demo.getId(), TODAY);
        long expenses = count("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :uid");
        List<String> titles = titles();

        // 테스트 유저가 이것저것 바꾼다
        TravelPlan first = em.createQuery("SELECT p FROM TravelPlan p WHERE p.user.id = :uid", TravelPlan.class)
                .setParameter("uid", demo.getId()).setMaxResults(1).getSingleResult();
        first.updateTitle("마음대로 바꾼 제목");
        User managedDemo = em.find(User.class, demo.getId());
        TravelPlan extra = TravelPlan.builder().user(managedDemo).title("새로 만든 일정").destination("강릉")
                .startDate(TODAY).endDate(TODAY).build();
        em.persist(extra);
        em.persist(Expense.builder().plan(extra).user(managedDemo).category("FOOD").description("추가")
                .amount(1000L).isEstimated(false).expenseDate(TODAY).build());
        Post demoPost = em.createQuery("SELECT p FROM Post p WHERE p.user.id = :uid", Post.class)
                .setParameter("uid", demo.getId()).setMaxResults(1).getSingleResult();
        // 다른 회원이 테스트 유저 글에 댓글을 단 경우도 지워져야 글을 지울 수 있다
        em.persist(PostComment.builder().post(demoPost).user(em.find(User.class, other.getId()))
                .content("댓글").status("ACTIVE").build());
        // 테스트 유저가 남의 글에 댓글
        em.persist(PostComment.builder().post(em.find(Post.class, otherPost.getId())).user(managedDemo)
                .content("테스트 댓글").status("ACTIVE").build());
        em.flush();
        em.clear();

        seedService.reset(demo.getId(), TODAY);

        assertThat(titles()).containsExactlyInAnyOrderElementsOf(titles);
        assertThat(count("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :uid")).isEqualTo(expenses);
        assertThat(count("SELECT COUNT(c) FROM PostComment c WHERE c.user.id = :uid")).isZero();

        // 다른 회원 쪽은 그대로
        assertThat(em.find(TravelPlan.class, otherPlan.getId())).isNotNull();
        assertThat(em.find(Post.class, otherPost.getId())).isNotNull();
        assertThat(em.find(User.class, other.getId()).getName()).isEqualTo("user01");
    }

    @Test
    @DisplayName("관리자로 승격·정지돼 있던 테스트 계정도 로그인 때 원래 권한과 상태로 돌아간다")
    void restoreAccountFixesRoleAndStatus() {
        em.createQuery("UPDATE User u SET u.role = 'ADMIN', u.status = 'SUSPENDED', u.name = '바뀐 이름' WHERE u.id = :uid")
                .setParameter("uid", demo.getId()).executeUpdate();

        seedService.restoreAccount(demo.getId(), "USER", DemoAccountService.USER_NAME);
        em.clear();

        User u = em.find(User.class, demo.getId());
        assertThat(u.getRole()).isEqualTo("USER");
        assertThat(u.getStatus()).isEqualTo("ACTIVE");
        assertThat(u.getName()).isEqualTo(DemoAccountService.USER_NAME);
    }

    private List<String> titles() {
        return em.createQuery("SELECT p.title FROM TravelPlan p WHERE p.user.id = :uid", String.class)
                .setParameter("uid", demo.getId()).getResultList();
    }

    @SuppressWarnings("unused")
    private static LocalDateTime noon() { return TODAY.atTime(12, 0); }
}
