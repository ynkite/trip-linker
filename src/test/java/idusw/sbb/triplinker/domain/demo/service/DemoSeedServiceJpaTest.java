package idusw.sbb.triplinker.domain.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import idusw.sbb.triplinker.domain.expense.entity.Expense;
import idusw.sbb.triplinker.domain.place.entity.Place;
import idusw.sbb.triplinker.domain.place.entity.PlaceCategory;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.post.entity.PlaceReview;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    private static final ObjectMapper JSON = new ObjectMapper();

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

    private List<TravelPlan> plans() {
        return em.createQuery("SELECT p FROM TravelPlan p WHERE p.user.id = :uid", TravelPlan.class)
                .setParameter("uid", demo.getId()).getResultList();
    }

    private TravelPlan plan(String destination) {
        return plans().stream().filter(p -> destination.equals(p.getDestination())).findFirst().orElseThrow();
    }

    /** 경로 JSON 의 날짜별 [장소 이름 → 평점 글]. 이동 줄은 뺀다. */
    private List<Map<String, String>> starsByDay(TravelPlan plan) throws Exception {
        List<Map<String, String>> days = new ArrayList<>();
        for (JsonNode day : JSON.readTree(plan.getRouteJson())) {
            Map<String, String> m = new HashMap<>();
            for (JsonNode p : day.get("places")) {
                if (p.has("type")) m.put(p.get("name").asText(), p.get("stars").asText());
            }
            days.add(m);
        }
        return days;
    }

    private static double ratingOf(String stars) {
        return Double.parseDouble(stars.substring(stars.lastIndexOf(' ') + 1));
    }

    private long reviewCount(String placeName) {
        return em.createQuery("SELECT COUNT(r) FROM PlaceReview r WHERE r.place.name = :n", Long.class)
                .setParameter("n", placeName).getSingleResult();
    }

    @Test
    @DisplayName("되돌리면 일정 4개(부산·속초·대구·대전), 가계부, 후기 3개, 스크랩이 들어간다")
    void seedsExampleData() throws Exception {
        seedService.reset(demo.getId(), TODAY);

        List<TravelPlan> plans = plans();
        assertThat(plans).hasSize(4);
        assertThat(plans).extracting(TravelPlan::getDestination)
                .containsExactlyInAnyOrder("부산", "속초", "대구", "대전");
        assertThat(plans).allMatch(p -> "FIXED".equals(p.getStatus()) && p.getForm() != null);
        // 지난 여행과 다가오는 여행이 섞여 있다
        assertThat(plans).anyMatch(p -> p.getEndDate().isBefore(TODAY));
        assertThat(plans).anyMatch(p -> p.getStartDate().isAfter(TODAY));
        // 만든 시각은 과거로 돌려 둔다(관리자 '오늘 새 일정' 통계에 안 잡히게)
        assertThat(plans).allMatch(p -> p.getCreatedAt().isBefore(TODAY.atStartOfDay()));

        // 지도: 모든 장소에 좌표가 있고 장소 사이에 이동 줄이 있다
        JsonNode route = JSON.readTree(plan("부산").getRouteJson());
        assertThat(route).hasSize(2);
        JsonNode day1 = route.get(0).get("places");
        assertThat(day1.get(0).get("name").asText()).isEqualTo("해운대블루라인파크 미포정거장");
        assertThat(day1.get(0).get("lat").isNumber()).isTrue();
        assertThat(day1.get(0).get("isFound").asBoolean()).isTrue();
        assertThat(day1.get(1).has("transit")).isTrue();
        // 가까운 곳은 걸어서(0원), 수영강을 건너는 둘째 날 첫 이동은 대중교통
        assertThat(day1.get(9).get("transit").asText()).startsWith("🚶 도보").endsWith("₩0");
        assertThat(route.get(1).get("places").get(1).get("transit").asText()).startsWith("🚌 대중교통");

        assertThat(count("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :uid")).isGreaterThanOrEqualTo(20);
        assertThat(count("SELECT COUNT(p) FROM Post p WHERE p.user.id = :uid")).isEqualTo(3);
        assertThat(count("SELECT COUNT(s) FROM PostScrap s WHERE s.user.id = :uid")).isEqualTo(1);
        assertThat(count("SELECT COUNT(s) FROM Scrap s WHERE s.user.id = :uid")).isEqualTo(3);
    }

    @Test
    @DisplayName("지난 여행도 가계부 예상 총액이 0 이 아니다(떠나기 전에 낸 예상 금액과 실제 지출이 함께 있다)")
    void everyTripHasEstimatedAmount() {
        seedService.reset(demo.getId(), TODAY);

        for (TravelPlan p : plans()) {
            Long est = em.createQuery("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.plan.id = :id AND e.isEstimated = true", Long.class)
                    .setParameter("id", p.getId()).getSingleResult();
            assertThat(est).as(p.getDestination() + " 예상 총액").isPositive();
            if (p.getEndDate().isBefore(TODAY)) {
                Long act = em.createQuery("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.plan.id = :id AND e.isEstimated = false", Long.class)
                        .setParameter("id", p.getId()).getSingleResult();
                assertThat(act).as(p.getDestination() + " 실제 지출").isPositive();
            }
        }
    }

    @Test
    @DisplayName("대표 일정(부산)의 모든 장소에 평점이 내려오고, 평균은 3 이상이며 값이 두 가지 이상이다")
    void showcaseTripHasRatingsEverywhere() throws Exception {
        seedService.reset(demo.getId(), TODAY);

        List<Double> values = new ArrayList<>();
        for (Map<String, String> day : starsByDay(plan("부산"))) {
            day.forEach((name, stars) -> {
                assertThat(stars).as(name).startsWith("★").doesNotContain("평점 정보 없음");
                values.add(ratingOf(stars));
            });
        }
        assertThat(values).hasSize(11).allMatch(v -> v >= 3.0 && v <= 5.0);
        assertThat(new HashSet<>(values)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(values).noneMatch(v -> v == 5.0);
    }

    @Test
    @DisplayName("나머지 일정도 날마다 두 곳 이상 평점이 보인다")
    void otherTripsHaveRatingsEachDay() throws Exception {
        seedService.reset(demo.getId(), TODAY);
        for (String dest : List.of("속초", "대구", "대전")) {
            for (Map<String, String> day : starsByDay(plan(dest))) {
                assertThat(day.values().stream().filter(s -> s.startsWith("★")).count())
                        .as(dest + " " + day.keySet()).isGreaterThanOrEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("후기 전용 테스트 계정 둘이 생기고(이름에 '테스트'), 다시 되돌려도 별점 수가 같다")
    void reviewerAccountsAndIdempotentRatings() {
        seedService.reset(demo.getId(), TODAY);
        List<User> reviewers = em.createQuery("SELECT u FROM User u WHERE u.username LIKE 'demo_review_%'", User.class)
                .getResultList();
        assertThat(reviewers).hasSize(2).allMatch(u -> u.getName().startsWith("테스트") && "USER".equals(u.getRole()));
        long total = em.createQuery("SELECT COUNT(r) FROM PlaceReview r", Long.class).getSingleResult();
        assertThat(total).isEqualTo(DemoSeedData.RATINGS.stream()
                .filter(r -> !DemoSeedData.USER.equals(r.reviewer())
                        || DemoSeedData.TRIPS.stream().anyMatch(t -> t.key().equals(r.tripKey()) && t.isPast(TODAY)))
                .count());
        // 테스트 유저 별점은 그 여행 후기 글에 붙는다
        assertThat(count("SELECT COUNT(r) FROM PlaceReview r WHERE r.user.id = :uid AND r.post IS NULL")).isZero();

        // 관리자가 후기 계정을 정지·개명해 두어도 다음 되돌리기에서 돌아온다
        em.createQuery("UPDATE User u SET u.status = 'SUSPENDED', u.name = '바뀐 이름' WHERE u.username = 'demo_review_a'")
                .executeUpdate();
        em.clear();
        seedService.reset(demo.getId(), TODAY);

        assertThat(em.createQuery("SELECT COUNT(r) FROM PlaceReview r", Long.class).getSingleResult()).isEqualTo(total);
        assertThat(em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username LIKE 'demo_review_%'", Long.class)
                .getSingleResult()).isEqualTo(2);
        User a = em.createQuery("SELECT u FROM User u WHERE u.username = 'demo_review_a'", User.class).getSingleResult();
        assertThat(a.getStatus()).isEqualTo("ACTIVE");
        assertThat(a.getName()).isEqualTo("테스트 리뷰어 A");
    }

    @Test
    @DisplayName("다른 회원 후기가 이미 있는 장소에는 별점을 더하지 않고, 화면 평점은 그 회원 후기 평균이다")
    void communityRatedPlacesAreLeftAlone() throws Exception {
        Place galbi = Place.builder().name("해운대암소갈비집").category(PlaceCategory.RESTAURANT).build();
        em.persist(galbi);
        User managedOther = em.find(User.class, other.getId());
        for (int stars : new int[]{5, 4, 4, 5}) {
            em.persist(PlaceReview.builder().place(galbi).user(managedOther).rating(stars).comment("맛있어요").build());
        }
        em.flush();

        seedService.reset(demo.getId(), TODAY);

        assertThat(reviewCount("해운대암소갈비집")).isEqualTo(4);
        assertThat(starsByDay(plan("부산")).get(0).get("해운대암소갈비집")).isEqualTo("★★★★☆ 4.5");
        // 장소 행이 하나만 있다(이름이 같은 장소를 새로 만들지 않음)
        assertThat(em.createQuery("SELECT COUNT(p) FROM Place p WHERE p.name = '해운대암소갈비집'", Long.class)
                .getSingleResult()).isEqualTo(1);
        // 장소당 별점은 3개를 넘지 않는다(인기 장소 후기 수 순위 위로 오르지 않게)
        List<Long> perPlace = em.createQuery("SELECT COUNT(r) FROM PlaceReview r WHERE r.user.id <> :o GROUP BY r.place.id", Long.class)
                .setParameter("o", other.getId()).getResultList();
        assertThat(perPlace).allMatch(c -> c <= 3);
    }

    @Test
    @DisplayName("테스트 글·별점은 다른 회원 글·후기보다 앞 날짜라 최신순 위 세 자리에 오지 않는다")
    void seededContentStaysBelowOthers() {
        // 다른 회원이 어제 글 셋과 후기 하나를 남겼다고 치자
        User managedOther = em.find(User.class, other.getId());
        List<Long> recentIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Post p = Post.builder().user(managedOther).title("최근 글" + i).content("내용").isPublic(true).build();
            em.persist(p);
            recentIds.add(p.getId());
        }
        Place place = em.createQuery("SELECT p FROM Place p WHERE p.name = '장소0'", Place.class).getSingleResult();
        PlaceReview review = PlaceReview.builder().place(place).user(managedOther).rating(4).comment("좋아요").build();
        em.persist(review);
        em.flush();
        LocalDateTime yesterday = TODAY.minusDays(1).atTime(9, 0);
        em.createQuery("UPDATE Post p SET p.createdAt = :at WHERE p.id IN :ids")
                .setParameter("at", yesterday).setParameter("ids", recentIds).executeUpdate();
        em.createQuery("UPDATE Post p SET p.createdAt = :at WHERE p.id = :id")
                .setParameter("at", yesterday.minusDays(30)).setParameter("id", otherPost.getId()).executeUpdate();
        em.createQuery("UPDATE PlaceReview r SET r.createdAt = :at WHERE r.id = :id")
                .setParameter("at", yesterday).setParameter("id", review.getId()).executeUpdate();
        em.clear();

        seedService.reset(demo.getId(), TODAY);

        List<Post> top3 = em.createQuery("SELECT p FROM Post p WHERE p.isPublic = true ORDER BY p.createdAt DESC", Post.class)
                .setMaxResults(3).getResultList();
        assertThat(top3).extracting(Post::getId).containsExactlyInAnyOrderElementsOf(recentIds);
        LocalDateTime newestSeeded = em.createQuery(
                "SELECT MAX(r.createdAt) FROM PlaceReview r WHERE r.user.id <> :o", LocalDateTime.class)
                .setParameter("o", other.getId()).getSingleResult();
        assertThat(newestSeeded).isBefore(yesterday);
        // 테스트 유저 후기 글은 여행이 끝난 뒤 날짜다
        TravelPlan busan = plan("부산");
        List<LocalDateTime> busanPosts = em.createQuery(
                "SELECT p.createdAt FROM Post p WHERE p.plan.id = :pid", LocalDateTime.class)
                .setParameter("pid", busan.getId()).getResultList();
        assertThat(busanPosts).isNotEmpty().allMatch(at -> at.toLocalDate().isAfter(busan.getEndDate()));
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
        // 테스트 유저가 별점을 하나 더 남김
        Place someplace = em.createQuery("SELECT p FROM Place p WHERE p.name = '장소1'", Place.class).getSingleResult();
        em.persist(PlaceReview.builder().place(someplace).user(managedDemo).rating(3).comment("추가 별점").build());
        em.flush();
        em.clear();

        seedService.reset(demo.getId(), TODAY);

        assertThat(titles()).containsExactlyInAnyOrderElementsOf(titles);
        assertThat(count("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :uid")).isEqualTo(expenses);
        assertThat(count("SELECT COUNT(c) FROM PostComment c WHERE c.user.id = :uid")).isZero();
        assertThat(reviewCount("장소1")).isZero();

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
}
