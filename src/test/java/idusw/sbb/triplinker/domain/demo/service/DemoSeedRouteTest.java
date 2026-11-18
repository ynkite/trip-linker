package idusw.sbb.triplinker.domain.demo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 예시 일정의 동선 검사. 누가 DemoSeedData 의 장소를 바꾸면 여기서 걸린다.
 *
 * 거리는 좌표 사이 직선거리(km). 도시 일정은 이웃 장소 6km·하루 20km 안,
 * 차로 도는 강원 일정(longDrive)은 이웃 12km·하루 30km 안을 기준으로 본다.
 */
class DemoSeedRouteTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 19);

    private static double km(DemoSeedData.Stop a, DemoSeedData.Stop b) {
        return DemoSeedService.haversineKm(a.lat(), a.lng(), b.lat(), b.lng());
    }

    private static double length(List<DemoSeedData.Stop> s) {
        double t = 0;
        for (int i = 0; i + 1 < s.size(); i++) t += km(s.get(i), s.get(i + 1));
        return t;
    }

    @Test
    @DisplayName("일정은 넷이고 지난 여행 둘, 다가오는 여행 둘이며 제주는 없다")
    void fourTripsPastAndUpcoming() {
        assertThat(DemoSeedData.TRIPS).hasSize(4);
        for (LocalDate today : List.of(TODAY, TODAY.plusYears(1).withMonth(3), LocalDate.of(2027, 7, 1))) {
            assertThat(DemoSeedData.TRIPS.stream().filter(t -> t.isPast(today))).hasSize(2);
            assertThat(DemoSeedData.TRIPS.stream().filter(t -> t.startDate(today).isAfter(today))).hasSize(2);
        }
        assertThat(DemoSeedData.TRIPS).noneMatch(t -> t.destination().contains("제주"));
        // 고정 경로 모드의 원본 탐색 키워드와 겹치지 않게
        assertThat(DemoSeedData.TRIPS).noneMatch(t -> t.destination().contains("해운대"));
        assertThat(DemoSeedData.TRIPS).anyMatch(t -> t.key().equals(DemoSeedData.SHOWCASE_TRIP));
    }

    @Test
    @DisplayName("하루 4~6곳, 시간이 차례로 가고, 식당이 연달아 둘 오지 않으며, 숙소는 묵는 날 마지막과 다음 날 처음에만")
    void dayShape() {
        for (DemoSeedData.Trip trip : DemoSeedData.TRIPS) {
            for (int d = 0; d < trip.days().size(); d++) {
                List<DemoSeedData.Stop> s = trip.days().get(d).stops();
                String where = trip.key() + " Day" + (d + 1);
                assertThat(s.size()).as(where + " 장소 수").isBetween(4, 6);
                for (int i = 0; i + 1 < s.size(); i++) {
                    assertThat(LocalTime.parse(s.get(i).time()))
                            .as(where + " 시간 순서").isBefore(LocalTime.parse(s.get(i + 1).time()));
                    assertThat("food".equals(s.get(i).type()) && "food".equals(s.get(i + 1).type()))
                            .as(where + " 식당 연달아 " + s.get(i).name()).isFalse();
                    if (i > 0) assertThat(s.get(i).type()).as(where + " 숙소는 처음과 마지막에만").isNotEqualTo("stay");
                }
                assertThat(s).as(where + " 관광지만 있는 날").anyMatch(x -> !"tour".equals(x.type()));
                boolean lastDay = d == trip.days().size() - 1;
                if (d > 0) {
                    List<DemoSeedData.Stop> prev = trip.days().get(d - 1).stops();
                    assertThat(s.get(0)).as(where + " 전날 숙소에서 시작")
                            .extracting(DemoSeedData.Stop::type, DemoSeedData.Stop::name)
                            .containsExactly("stay", prev.get(prev.size() - 1).name());
                } else {
                    assertThat(s.get(0).type()).as(where + " 첫날은 숙소로 시작하지 않음").isNotEqualTo("stay");
                }
                if (!lastDay) {
                    assertThat(s.get(s.size() - 1).type()).as(where + " 묵는 날은 숙소로 끝남").isEqualTo("stay");
                }
            }
        }
    }

    @Test
    @DisplayName("이웃 장소 거리와 하루 합계가 기준 안이고, 다음 날은 숙소 가까이에서 시작한다")
    void distances() {
        for (DemoSeedData.Trip trip : DemoSeedData.TRIPS) {
            double maxLeg = trip.longDrive() ? 12.0 : 6.0;
            double maxDay = trip.longDrive() ? 30.0 : 20.0;
            DemoSeedData.Stop prevStay = null;
            for (int d = 0; d < trip.days().size(); d++) {
                List<DemoSeedData.Stop> s = trip.days().get(d).stops();
                String where = trip.key() + " Day" + (d + 1);
                for (int i = 0; i + 1 < s.size(); i++) {
                    double k = km(s.get(i), s.get(i + 1));
                    assertThat(k).as(where + " " + s.get(i).name() + "→" + s.get(i + 1).name())
                            .isGreaterThan(0.1).isLessThanOrEqualTo(maxLeg);
                }
                assertThat(length(s)).as(where + " 하루 합계").isLessThanOrEqualTo(maxDay);
                if (prevStay != null) {
                    assertThat(km(prevStay, s.get(0))).as(where + " 숙소에서 첫 장소").isLessThanOrEqualTo(maxLeg);
                }
                prevStay = s.get(s.size() - 1);
            }
        }
    }

    @Test
    @DisplayName("순서를 바꿔도 하루 거리가 10% 넘게 줄지 않고(처음·끝 고정), 선이 서로 엇갈리지 않는다")
    void orderIsNearOptimalAndLinesDoNotCross() {
        for (DemoSeedData.Trip trip : DemoSeedData.TRIPS) {
            for (int d = 0; d < trip.days().size(); d++) {
                List<DemoSeedData.Stop> s = trip.days().get(d).stops();
                String where = trip.key() + " Day" + (d + 1);
                double best = bestLength(s);
                assertThat(length(s)).as(where + " 총거리 / 최단").isLessThanOrEqualTo(best * 1.10);
                for (int i = 0; i + 1 < s.size(); i++) {
                    for (int j = i + 2; j + 1 < s.size(); j++) {
                        assertThat(crosses(s.get(i), s.get(i + 1), s.get(j), s.get(j + 1)))
                                .as(where + " 선 엇갈림 " + s.get(i).name() + "-" + s.get(j).name()).isFalse();
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("일정 전체가 지도 한 화면(도시 한두 구 크기)에 들어온다")
    void wholeTripFitsOneScreen() {
        for (DemoSeedData.Trip trip : DemoSeedData.TRIPS) {
            List<DemoSeedData.Stop> all = trip.days().stream().flatMap(d -> d.stops().stream()).toList();
            double limit = trip.longDrive() ? 15.0 : 7.0;
            for (DemoSeedData.Stop a : all) {
                for (DemoSeedData.Stop b : all) {
                    assertThat(km(a, b)).as(trip.key() + " " + a.name() + "↔" + b.name()).isLessThanOrEqualTo(limit);
                }
            }
        }
    }

    @Test
    @DisplayName("별점은 3~5, 장소마다 2~3개, 같은 사람이 한 장소에 두 번 쓰지 않고, 소감 문장이 겹치지 않는다")
    void ratingsShape() {
        Set<String> seen = new HashSet<>();
        Set<String> comments = new HashSet<>();
        for (DemoSeedData.Rating r : DemoSeedData.RATINGS) {
            assertThat(r.stars()).isBetween(3, 5);
            assertThat(seen.add(r.tripKey() + "|" + r.place() + "|" + r.reviewer())).as("중복 " + r).isTrue();
            assertThat(comments.add(r.comment())).as("같은 문장 " + r.comment()).isTrue();
            DemoSeedData.Trip trip = DemoSeedData.TRIPS.stream().filter(t -> t.key().equals(r.tripKey())).findFirst().orElseThrow();
            assertThat(trip.days().stream().flatMap(d -> d.stops().stream()).map(DemoSeedData.Stop::name))
                    .as("일정에 있는 장소 " + r.place()).contains(r.place());
            if (DemoSeedData.USER.equals(r.reviewer())) {
                assertThat(trip.isPast(TODAY)).as("테스트 유저는 지난 여행에만 " + r).isTrue();
            }
        }
        // 장소마다 2~3개, 평균은 3 이상이고 장소끼리 값이 한 가지가 아니다
        List<Double> avgs = new ArrayList<>();
        for (String place : DemoSeedData.RATINGS.stream().map(DemoSeedData.Rating::place).distinct().toList()) {
            List<Integer> stars = DemoSeedData.RATINGS.stream().filter(r -> r.place().equals(place))
                    .map(DemoSeedData.Rating::stars).toList();
            assertThat(stars.size()).as(place + " 후기 수").isBetween(2, 3);
            assertThat(new HashSet<>(stars)).as(place + " 별점이 모두 같음").hasSizeGreaterThan(1);
            avgs.add(stars.stream().mapToInt(Integer::intValue).average().orElse(0));
        }
        assertThat(avgs).allMatch(a -> a >= 3.0);
        assertThat(new HashSet<>(avgs)).hasSizeGreaterThan(2);
        // 대표 일정은 모든 장소에 별점이 준비돼 있다
        DemoSeedData.Trip show = DemoSeedData.TRIPS.stream()
                .filter(t -> t.key().equals(DemoSeedData.SHOWCASE_TRIP)).findFirst().orElseThrow();
        for (DemoSeedData.Day day : show.days()) {
            for (DemoSeedData.Stop s : day.stops()) {
                assertThat(DemoSeedData.RATINGS).as("대표 일정 별점 " + s.name())
                        .anyMatch(r -> r.place().equals(s.name()));
            }
        }
    }

    @Test
    @DisplayName("평점 글 모양: 4.33 → ★★★★☆ 4.3, 3.67 → ★★★☆☆ 3.7")
    void starsText() {
        assertThat(DemoSeedService.starsText(13 / 3.0)).isEqualTo("★★★★☆ 4.3");
        assertThat(DemoSeedService.starsText(11 / 3.0)).isEqualTo("★★★☆☆ 3.7");
        assertThat(DemoSeedService.starsText(4.5)).isEqualTo("★★★★☆ 4.5");
        assertThat(DemoSeedService.starsText(5.0)).isEqualTo("★★★★★ 5.0");
    }

    /** 처음과 끝을 고정하고 가운데 장소 순서를 모두 바꿔 본 최단 거리. */
    private static double bestLength(List<DemoSeedData.Stop> s) {
        if (s.size() <= 3) return length(s);
        List<DemoSeedData.Stop> mid = new ArrayList<>(s.subList(1, s.size() - 1));
        double[] best = {Double.MAX_VALUE};
        permute(mid, 0, p -> {
            List<DemoSeedData.Stop> route = new ArrayList<>();
            route.add(s.get(0));
            route.addAll(p);
            route.add(s.get(s.size() - 1));
            best[0] = Math.min(best[0], length(route));
        });
        return best[0];
    }

    private static void permute(List<DemoSeedData.Stop> a, int k, java.util.function.Consumer<List<DemoSeedData.Stop>> f) {
        if (k == a.size()) { f.accept(a); return; }
        for (int i = k; i < a.size(); i++) {
            java.util.Collections.swap(a, k, i);
            permute(a, k + 1, f);
            java.util.Collections.swap(a, k, i);
        }
    }

    private static double ccw(DemoSeedData.Stop a, DemoSeedData.Stop b, DemoSeedData.Stop c) {
        return (c.lat() - a.lat()) * (b.lng() - a.lng()) - (b.lat() - a.lat()) * (c.lng() - a.lng());
    }

    private static boolean crosses(DemoSeedData.Stop p1, DemoSeedData.Stop p2, DemoSeedData.Stop p3, DemoSeedData.Stop p4) {
        return ccw(p1, p3, p4) * ccw(p2, p3, p4) < 0 && ccw(p1, p2, p3) * ccw(p1, p2, p4) < 0;
    }
}
