package idusw.sbb.triplinker.domain.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import idusw.sbb.triplinker.domain.expense.entity.Expense;
import idusw.sbb.triplinker.domain.place.entity.Place;
import idusw.sbb.triplinker.domain.plan.entity.PlanInputForm;
import idusw.sbb.triplinker.domain.plan.entity.TravelPlan;
import idusw.sbb.triplinker.domain.place.entity.PlaceCategory;
import idusw.sbb.triplinker.domain.post.entity.PlaceReview;
import idusw.sbb.triplinker.domain.post.entity.Post;
import idusw.sbb.triplinker.domain.post.entity.PostScrap;
import idusw.sbb.triplinker.domain.user.entity.Scrap;
import idusw.sbb.triplinker.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 테스트 유저 데이터를 시드 상태로 되돌린다.
 *
 * 테스트 유저와 후기 전용 테스트 계정(DemoSeedData.REVIEWERS)이 만든 것(일정, 입력폼, 가계부, 공유 멤버, 챗봇 기록, 글과 그 글에 달린 것,
 * 장소 별점, 좋아요, 스크랩, 신고, 알림)만 지우고 다시 넣는다. 다른 회원 계정과 그 회원의 글·일정·별점은 지우지 않는다.
 * 장소(PLACES) 행은 여러 회원이 같이 쓰므로 지우지 않는다. 예시 일정의 장소가 없으면 이름·좌표로 한 행씩 만든다.
 * 다만 아래 두 경우는 테스트 유저 쪽을 지우기 위해 다른 행의 연결만 끊는다.
 *   - 다른 회원 글이 테스트 유저 일정을 연결해 둔 경우: 그 글의 plan_id 를 비운다.
 *   - 큐레이션이 테스트 유저 일정을 연결해 둔 경우: 큐레이션의 plan_id 를 비운다.
 * 테스트 유저 글에 다른 회원이 단 댓글·좋아요·스크랩·신고는 글과 함께 지워진다.
 *
 * 한 트랜잭션으로 돈다. 중간에 실패하면 모두 되돌아가고 기존 데이터가 그대로 남는다.
 */
@Service
@RequiredArgsConstructor
public class DemoSeedService {

    private static final DateTimeFormatter MD = DateTimeFormatter.ofPattern("MM/dd");
    private static final String[] WEEK = {"월", "화", "수", "목", "금", "토", "일"};
    /** 다른 회원 글·후기가 하나도 없을 때의 날짜 상한(사실상 없음). */
    private static final LocalDateTime NO_LIMIT = LocalDateTime.of(9999, 1, 1, 0, 0);
    /** 대중교통 일정에서 이 거리(도로 보정 뒤) 안쪽은 걸어서 가는 것으로 적는다. */
    private static final double WALK_KM = 1.5;

    private final EntityManager em;
    private final ObjectMapper objectMapper;

    @Transactional
    public void reset(Long userId, LocalDate today) {
        User user = em.find(User.class, userId);
        if (user == null) {
            throw new IllegalStateException("테스트 유저를 찾을 수 없습니다.");
        }
        Map<String, Long> reviewers = ensureReviewers();
        wipe(userId);
        for (Long rid : reviewers.values()) {
            wipe(rid);
        }
        seed(user, reviewers, today);
    }

    /**
     * 후기 전용 테스트 계정(로그인 불가)을 없으면 만들고, 이름·권한·상태를 처음 값으로 맞춘다.
     * 같은 장소에 별점이 여럿 달린 모습을 보여 주려면 쓴 사람이 여럿이어야 해서 둔다.
     */
    Map<String, Long> ensureReviewers() {
        Map<String, Long> ids = new HashMap<>();
        for (DemoSeedData.Reviewer r : DemoSeedData.REVIEWERS) {
            List<User> found = em.createQuery("SELECT u FROM User u WHERE u.username = :n", User.class)
                    .setParameter("n", r.username()).setMaxResults(1).getResultList();
            User u;
            if (found.isEmpty()) {
                u = User.builder()
                        .username(r.username())
                        .passwordHash(DemoAccountService.unusablePasswordHash())
                        .name(r.name())
                        .email(r.email())
                        .region("서울")
                        .role("USER")
                        .status("ACTIVE")
                        .lastPwChangedAt(LocalDateTime.now())
                        .build();
                em.persist(u);
                em.flush();
            } else {
                u = found.get(0);
            }
            restoreAccount(u.getId(), "USER", r.name());
            ids.put(r.key(), u.getId());
        }
        return ids;
    }

    /**
     * 테스트 계정의 이름·권한·상태를 처음 값으로 되돌린다. 로그인할 때마다 부른다.
     * 누가 관리자 화면에서 테스트 계정을 승격·정지해 두었어도 여기서 원래대로 돌아간다
     * (refresh 토큰 재발급은 DB의 권한을 읽으므로 DB 값을 맞춰 두어야 한다).
     */
    @Transactional
    public void restoreAccount(Long userId, String role, String name) {
        em.createQuery("UPDATE User u SET u.name = :name, u.region = '서울', u.role = :role, "
                        + "u.status = 'ACTIVE', u.loginFailCount = 0, u.lockedUntil = NULL, "
                        + "u.gender = NULL, u.mbti = NULL, u.birthDate = NULL WHERE u.id = :uid")
                .setParameter("name", name)
                .setParameter("role", role)
                .setParameter("uid", userId)
                .executeUpdate();
    }

    // ───────────────────────── 지우기 ─────────────────────────

    void wipe(Long uid) {
        List<Long> planIds = em.createQuery(
                "SELECT p.id FROM TravelPlan p WHERE p.user.id = :uid", Long.class)
                .setParameter("uid", uid).getResultList();
        List<Long> postIds = em.createQuery(
                "SELECT p.id FROM Post p WHERE p.user.id = :uid", Long.class)
                .setParameter("uid", uid).getResultList();

        // 테스트 유저 글에 딸린 것
        if (!postIds.isEmpty()) {
            for (String q : List.of(
                    "DELETE FROM PostViewLog x WHERE x.post.id IN :ids",
                    "DELETE FROM Report x WHERE x.post.id IN :ids",
                    "DELETE FROM PostComment x WHERE x.post.id IN :ids",
                    "DELETE FROM PostLike x WHERE x.post.id IN :ids",
                    "DELETE FROM PostScrap x WHERE x.post.id IN :ids",
                    "DELETE FROM PostImage x WHERE x.post.id IN :ids",
                    "DELETE FROM PlaceReview x WHERE x.post.id IN :ids")) {
                em.createQuery(q).setParameter("ids", postIds).executeUpdate();
            }
        }

        // 테스트 유저가 다른 글에 남긴 좋아요는 지우기 전에 좋아요 수를 하나씩 되돌린다.
        em.createQuery("UPDATE Post p SET p.likeCount = p.likeCount - 1 "
                        + "WHERE p.likeCount > 0 AND p.id IN "
                        + "(SELECT l.post.id FROM PostLike l WHERE l.user.id = :uid)")
                .setParameter("uid", uid).executeUpdate();

        // 테스트 유저가 남긴 것
        for (String q : List.of(
                "DELETE FROM PostLike x WHERE x.user.id = :uid",
                "DELETE FROM PostScrap x WHERE x.user.id = :uid",
                "DELETE FROM PostViewLog x WHERE x.viewer.id = :uid",
                "DELETE FROM Report x WHERE x.reporter.id = :uid",
                "DELETE FROM PostComment x WHERE x.user.id = :uid",
                "DELETE FROM PlaceReview x WHERE x.user.id = :uid",
                "DELETE FROM Notification x WHERE x.user.id = :uid",
                "DELETE FROM Scrap x WHERE x.user.id = :uid",
                "DELETE FROM TripMember x WHERE x.user.id = :uid",
                "DELETE FROM Expense x WHERE x.user.id = :uid")) {
            em.createQuery(q).setParameter("uid", uid).executeUpdate();
        }

        if (!planIds.isEmpty()) {
            for (String q : List.of(
                    "UPDATE Post x SET x.plan = NULL WHERE x.plan.id IN :ids",
                    "UPDATE Curation x SET x.plan = NULL WHERE x.plan.id IN :ids",
                    "DELETE FROM Expense x WHERE x.plan.id IN :ids",
                    "DELETE FROM TripMember x WHERE x.travelPlan.id IN :ids",
                    "DELETE FROM ChatMessage x WHERE x.chatSession.id IN "
                            + "(SELECT s.id FROM ChatSession s WHERE s.planId IN :ids)",
                    "DELETE FROM ChatSession x WHERE x.planId IN :ids",
                    "UPDATE TravelPlan x SET x.form = NULL WHERE x.id IN :ids",
                    "DELETE FROM PlanInputForm x WHERE x.plan.id IN :ids")) {
                em.createQuery(q).setParameter("ids", planIds).executeUpdate();
            }
        }
        if (!postIds.isEmpty()) {
            em.createQuery("DELETE FROM Post x WHERE x.id IN :ids")
                    .setParameter("ids", postIds).executeUpdate();
        }
        if (!planIds.isEmpty()) {
            em.createQuery("DELETE FROM TravelPlan x WHERE x.id IN :ids")
                    .setParameter("ids", planIds).executeUpdate();
        }

        // 여러 사람이 같은 계정으로 들어오므로 로그인 때 토큰을 지우지 않는다. 만료된 것만 여기서 정리한다.
        em.createQuery("DELETE FROM RefreshToken x WHERE x.userId = :uid AND x.expiresAt < :now")
                .setParameter("uid", uid).setParameter("now", LocalDateTime.now())
                .executeUpdate();

        em.flush();
        em.clear();
    }

    // ───────────────────────── 넣기 ─────────────────────────

    void seed(User detachedUser, Map<String, Long> reviewerIds, LocalDate today) {
        User user = em.find(User.class, detachedUser.getId());
        Map<String, TravelPlan> plans = new HashMap<>();

        for (DemoSeedData.Trip trip : DemoSeedData.TRIPS) {
            LocalDate start = trip.startDate(today);
            TravelPlan plan = TravelPlan.builder()
                    .user(user)
                    .title(trip.title())
                    .destination(trip.destination())
                    .startDate(start)
                    .endDate(trip.endDate(today))
                    .isPublic(0)
                    .status("FIXED")
                    .build();
            em.persist(plan);

            PlanInputForm form = PlanInputForm.builder()
                    .plan(plan)
                    .user(user)
                    .departure("서울")
                    .transportType(trip.transport())
                    .accommodationType(trip.days().size() > 1 ? "호텔" : null)
                    .companionType(trip.companion())
                    .companionCount(trip.companionCount())
                    .travelStyles(trip.styles())
                    .scheduleDensity("여유롭게")
                    .budget(trip.budget())
                    .build();
            em.persist(form);
            plan.linkInputForm(form);

            boolean past = trip.isPast(today);
            for (int d = 0; d < trip.days().size(); d++) {
                for (DemoSeedData.Stop s : trip.days().get(d).stops()) {
                    if (s.amount() <= 0) continue;
                    em.persist(Expense.builder()
                            .plan(plan).user(user)
                            .category(expenseCategory(s.type()))
                            .description(s.name())
                            .amount(s.amount())
                            .isEstimated(!past)
                            .expenseDate(start.plusDays(d))
                            .build());
                }
            }
            plans.put(trip.key(), plan);
        }

        for (Object[] row : DemoSeedData.EXTRA_EXPENSES) {
            TravelPlan plan = plans.get((String) row[0]);
            em.persist(Expense.builder()
                    .plan(plan).user(user)
                    .category((String) row[2])
                    .description((String) row[3])
                    .amount((Long) row[4])
                    .isEstimated(false)
                    .expenseDate(plan.getStartDate().plusDays((Integer) row[1]))
                    .build());
        }

        List<Long> demoIds = new ArrayList<>(reviewerIds.values());
        demoIds.add(user.getId());

        // 다른 회원 글·후기보다 위로 올라오지 않게 할 시각의 상한
        LocalDateTime postCeiling = postCeiling(demoIds);
        LocalDateTime reviewCeiling = reviewCeiling(demoIds);

        Map<Post, LocalDateTime> postDates = new HashMap<>();
        Map<String, Post> routePosts = new HashMap<>();
        for (DemoSeedData.Review r : DemoSeedData.REVIEWS) {
            TravelPlan plan = plans.get(r.tripKey());
            boolean route = "ROUTE".equals(r.category());
            Post post = Post.builder()
                    .user(user)
                    .plan(route ? plan : null)
                    .title(r.title())
                    .content(r.content())
                    .styleTags(r.styleTags())
                    .category(r.category())
                    .isPublic(true)
                    .build();
            em.persist(post);
            if (route) routePosts.put(r.tripKey(), post);
            int n = postDates.size();
            LocalDateTime at = plan.getEndDate().plusDays(2).atTime(20, 0).plusMinutes(n);
            postDates.put(post, earlier(at, postCeiling.minusMinutes(n)));
        }

        // 장소 별점. 다른 회원 후기가 이미 있는 장소는 건너뛴다(운영의 장소 순위·평균을 건드리지 않게).
        Map<String, User> writers = new HashMap<>();
        writers.put(DemoSeedData.USER, user);
        reviewerIds.forEach((k, id) -> writers.put(k, em.find(User.class, id)));
        Map<String, Boolean> communityRated = new HashMap<>();
        Map<PlaceReview, LocalDateTime> reviewDates = new HashMap<>();
        for (DemoSeedData.Rating r : DemoSeedData.RATINGS) {
            DemoSeedData.Trip trip = trip(r.tripKey());
            Place place = findOrCreatePlace(stop(trip, r.place()));
            boolean rated = communityRated.computeIfAbsent(r.place(), name -> em.createQuery(
                    "SELECT COUNT(x) FROM PlaceReview x WHERE x.place.id = :pid AND x.user.id NOT IN :ids", Long.class)
                    .setParameter("pid", place.getId()).setParameter("ids", demoIds)
                    .getSingleResult() > 0);
            if (rated) continue;
            boolean byUser = DemoSeedData.USER.equals(r.reviewer());
            if (byUser && !trip.isPast(today)) continue; // 테스트 유저는 다녀온 곳에만 쓴다
            Post post = byUser ? routePosts.get(r.tripKey()) : null;
            PlaceReview review = PlaceReview.builder()
                    .place(place)
                    .post(post)
                    .user(writers.get(r.reviewer()))
                    .rating(Math.max(3, Math.min(5, r.stars())))
                    .comment(r.comment())
                    .build();
            em.persist(review);
            int n = reviewDates.size();
            LocalDateTime at = post != null
                    ? postDates.get(post)
                    : today.minusDays(r.daysAgo()).atTime(19, 0).plusMinutes(n);
            reviewDates.put(review, earlier(at, reviewCeiling.minusMinutes(n)));
        }

        // 다른 회원의 공개 글 두 개를 스크랩해 둔다(남의 행은 읽기만 한다).
        List<Post> others = em.createQuery(
                "SELECT p FROM Post p WHERE p.user.id NOT IN :ids AND p.status = 'ACTIVE' "
                        + "AND p.isPublic = true ORDER BY p.id ASC", Post.class)
                .setParameter("ids", demoIds)
                .setMaxResults(2).getResultList();
        for (Post p : others) {
            em.persist(PostScrap.builder().user(user).post(p).category(p.getCategory()).build());
        }
        // 다가오는 여행에서 갈 장소를 스크랩해 둔다.
        for (String name : DemoSeedData.PLACE_SCRAPS) {
            Place p = findOrCreatePlace(stopAnywhere(name));
            em.persist(Scrap.builder().user(user).placeId(p.getId()).category(placeCategory(p)).build());
        }

        em.flush();

        // 경로 JSON 은 별점이 다 들어간 뒤에 만든다. 장소 평점은 그 장소 후기 전체(다른 회원 것 포함)의 평균이다.
        Map<String, String> stars = starsByName();
        for (DemoSeedData.Trip trip : DemoSeedData.TRIPS) {
            TravelPlan plan = plans.get(trip.key());
            plan.setRouteJson(buildRouteJson(trip, plan.getStartDate(), stars));
        }
        em.flush();

        // 만든 시각을 여행 날짜에 맞춰 과거로 돌린다. 로그인할 때마다 관리자 통계의
        // '오늘 새 일정'이 늘거나 커뮤니티 맨 위로 테스트 글이 올라오지 않게 하려는 것이다.
        for (TravelPlan plan : plans.values()) {
            // 여행 2주 전에 짠 것으로. 다가오는 여행이라 그 날이 아직 안 왔으면 사흘 전으로.
            LocalDate created = plan.getStartDate().minusDays(14);
            if (!created.isBefore(today)) created = today.minusDays(3);
            em.createQuery("UPDATE TravelPlan p SET p.createdAt = :at WHERE p.id = :id")
                    .setParameter("at", created.atTime(21, 0))
                    .setParameter("id", plan.getId()).executeUpdate();
        }
        for (Map.Entry<Post, LocalDateTime> e : postDates.entrySet()) {
            em.createQuery("UPDATE Post p SET p.createdAt = :at, p.updatedAt = :at WHERE p.id = :id")
                    .setParameter("at", e.getValue())
                    .setParameter("id", e.getKey().getId()).executeUpdate();
        }
        for (Map.Entry<PlaceReview, LocalDateTime> e : reviewDates.entrySet()) {
            em.createQuery("UPDATE PlaceReview r SET r.createdAt = :at WHERE r.id = :id")
                    .setParameter("at", e.getValue())
                    .setParameter("id", e.getKey().getId()).executeUpdate();
        }
        em.clear();
    }

    /**
     * 테스트 글이 커뮤니티 최신순 위 세 자리에 들지 않도록, 다른 회원 공개 글 가운데
     * 세 번째로 최근 글보다 하루 앞을 상한으로 잡는다. 다른 글이 없으면 상한 없음.
     */
    private LocalDateTime postCeiling(List<Long> demoIds) {
        List<LocalDateTime> recent = em.createQuery(
                "SELECT p.createdAt FROM Post p WHERE p.user.id NOT IN :ids AND p.status = 'ACTIVE' "
                        + "AND p.isPublic = true AND p.createdAt IS NOT NULL ORDER BY p.createdAt DESC",
                        LocalDateTime.class)
                .setParameter("ids", demoIds).setMaxResults(3).getResultList();
        if (recent.isEmpty()) return NO_LIMIT;
        return recent.get(recent.size() - 1).minusDays(1);
    }

    /**
     * 장소 카드 최신순(장소별 가장 최근 후기 기준)에서 테스트 별점이 위로 오지 않도록,
     * 다른 회원 후기 가운데 가장 오래된 것보다 하루 앞을 상한으로 잡는다.
     */
    private LocalDateTime reviewCeiling(List<Long> demoIds) {
        List<LocalDateTime> oldest = em.createQuery(
                "SELECT r.createdAt FROM PlaceReview r WHERE r.user.id NOT IN :ids AND r.createdAt IS NOT NULL "
                        + "ORDER BY r.createdAt ASC", LocalDateTime.class)
                .setParameter("ids", demoIds).setMaxResults(1).getResultList();
        if (oldest.isEmpty()) return NO_LIMIT;
        return oldest.get(0).minusDays(1);
    }

    private static LocalDateTime earlier(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    /** 이름이 같은 장소가 있으면 그것을, 없으면 새로 만든다(PlaceService 와 같은 이름 기준). 지울 때 장소 행은 남긴다. */
    private Place findOrCreatePlace(DemoSeedData.Stop s) {
        List<Place> found = em.createQuery("SELECT p FROM Place p WHERE p.name = :n ORDER BY p.id ASC", Place.class)
                .setParameter("n", s.name()).setMaxResults(1).getResultList();
        if (!found.isEmpty()) return found.get(0);
        Place p = Place.builder()
                .name(s.name())
                .category(placeCategoryOf(s.type()))
                .latitude(BigDecimal.valueOf(s.lat()))
                .longitude(BigDecimal.valueOf(s.lng()))
                .build();
        em.persist(p);
        return p;
    }

    /** 일정에 들어간 장소 이름별 평점 글. 후기가 없는 장소는 넣지 않는다(화면에는 종전대로 '평점 정보 없음'). */
    Map<String, String> starsByName() {
        Map<String, String> out = new HashMap<>();
        for (DemoSeedData.Trip trip : DemoSeedData.TRIPS) {
            for (DemoSeedData.Day day : trip.days()) {
                for (DemoSeedData.Stop s : day.stops()) {
                    if (out.containsKey(s.name())) continue;
                    List<Long> ids = em.createQuery(
                            "SELECT p.id FROM Place p WHERE p.name = :n ORDER BY p.id ASC", Long.class)
                            .setParameter("n", s.name()).setMaxResults(1).getResultList();
                    if (ids.isEmpty()) continue;
                    Double avg = em.createQuery(
                            "SELECT AVG(r.rating) FROM PlaceReview r WHERE r.place.id = :pid", Double.class)
                            .setParameter("pid", ids.get(0)).getSingleResult();
                    if (avg != null && avg > 0) out.put(s.name(), starsText(avg));
                }
            }
        }
        return out;
    }

    /** 예: 4.33 → "★★★★☆ 4.3" (PlaceService 가 끝의 숫자를 평점으로 읽는 형식) */
    static String starsText(double avg) {
        double rounded = Math.round(avg * 10.0) / 10.0;
        int full = (int) Math.floor(rounded);
        return "★".repeat(full) + "☆".repeat(5 - full) + " " + String.format("%.1f", rounded);
    }

    private static DemoSeedData.Trip trip(String key) {
        return DemoSeedData.TRIPS.stream().filter(t -> t.key().equals(key)).findFirst()
                .orElseThrow(() -> new IllegalStateException("예시 일정이 없습니다: " + key));
    }

    private static DemoSeedData.Stop stop(DemoSeedData.Trip trip, String name) {
        return trip.days().stream().flatMap(d -> d.stops().stream()).filter(s -> s.name().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalStateException("예시 일정에 없는 장소입니다: " + name));
    }

    private static DemoSeedData.Stop stopAnywhere(String name) {
        return DemoSeedData.TRIPS.stream().flatMap(t -> t.days().stream()).flatMap(d -> d.stops().stream())
                .filter(s -> s.name().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalStateException("예시 일정에 없는 장소입니다: " + name));
    }

    /** AiRouteService 가 저장하는 것과 같은 모양의 경로 JSON. 장소 사이에 이동 줄을 넣는다. */
    String buildRouteJson(DemoSeedData.Trip trip, LocalDate start, Map<String, String> stars) {
        boolean car = !trip.transport().contains("대중교통");
        ArrayNode root = objectMapper.createArrayNode();
        for (int d = 0; d < trip.days().size(); d++) {
            LocalDate date = start.plusDays(d);
            List<DemoSeedData.Stop> stops = trip.days().get(d).stops();
            ObjectNode day = root.addObject();
            day.put("day", d + 1);
            day.put("label", "📅 Day " + (d + 1) + " · " + date.format(MD) + " (" + weekday(date.getDayOfWeek()) + ")");
            ArrayNode places = day.putArray("places");
            long total = 0;
            for (int i = 0; i < stops.size(); i++) {
                DemoSeedData.Stop s = stops.get(i);
                if (i > 0) {
                    long fare = appendTransit(places, stops.get(i - 1), s, car, trip.companionCount());
                    total += fare;
                }
                ObjectNode p = places.addObject();
                p.put("type", s.type());
                p.put("icon", icon(s.type()));
                p.put("name", s.name());
                p.put("sub", typeLabel(s.type()) + " · " + won(s.amount()));
                p.put("stars", stars.getOrDefault(s.name(), "평점 정보 없음"));
                p.put("key", trip.key() + "_d" + (d + 1) + "_" + (i + 1));
                p.put("time", s.time());
                p.put("lat", s.lat());
                p.put("lng", s.lng());
                p.put("isFound", true);
                total += s.amount();
            }
            day.put("budget", won(total));
        }
        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IllegalStateException("예시 경로를 만들지 못했습니다.", e);
        }
    }

    /** 직선거리에 도로 보정(1.3배)을 곱한 대략값. 차·대중교통 금액 계산식은 AiRouteService 와 같다. */
    private long appendTransit(ArrayNode places, DemoSeedData.Stop from, DemoSeedData.Stop to,
                               boolean car, int pax) {
        double km = Math.max(0.3, haversineKm(from.lat(), from.lng(), to.lat(), to.lng()) * 1.3);
        long min;
        long cost;
        String label;
        if (!car && km <= WALK_KM) {
            // 가까운 곳은 걸어서 간다(시속 4km). 금액 0원.
            min = Math.max(3, Math.round(km / 4.0 * 60.0));
            cost = 0;
            label = "🚶 도보";
        } else if (car) {
            min = Math.max(3, Math.round(km / 35.0 * 60.0));
            cost = Math.round(km / 10.0 * 2000.0);
            label = "🚗 자차";
        } else {
            min = Math.max(5, Math.round(km / 22.0 * 60.0));
            long perPerson = 1500 + (km > 10 ? Math.round((km - 10) * 100) : 0);
            cost = perPerson * Math.max(1, pax);
            label = "🚌 대중교통";
        }
        places.addObject().put("transit",
                String.format("%s · %.1fkm · 약 %d분 · %s", label, km, min, won(cost)));
        return cost;
    }

    static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }

    private static String won(long v) {
        return "₩" + String.format("%,d", v);
    }

    private static String weekday(DayOfWeek d) {
        return WEEK[d.getValue() - 1];
    }

    private static String icon(String type) {
        return switch (type) {
            case "stay" -> "🏨";
            case "food" -> "🍽️";
            case "cafe" -> "☕";
            default -> "📍";
        };
    }

    private static String typeLabel(String type) {
        return switch (type) {
            case "stay" -> "숙소";
            case "food" -> "맛집";
            case "cafe" -> "카페";
            default -> "관광지";
        };
    }

    private static String expenseCategory(String type) {
        return switch (type) {
            case "stay" -> "STAY";
            case "food" -> "FOOD";
            case "cafe" -> "CAFE";
            default -> "TOUR";
        };
    }

    private static PlaceCategory placeCategoryOf(String type) {
        return switch (type) {
            case "stay" -> PlaceCategory.ACCOMMODATION;
            case "food" -> PlaceCategory.RESTAURANT;
            case "cafe" -> PlaceCategory.CAFE;
            default -> PlaceCategory.ATTRACTION;
        };
    }

    private static String placeCategory(Place p) {
        if (p.getCategory() == null) return "TOUR";
        return switch (p.getCategory()) {
            case ACCOMMODATION -> "STAY";
            case RESTAURANT -> "FOOD";
            case CAFE -> "CAFE";
            case ATTRACTION -> "TOUR";
        };
    }
}
