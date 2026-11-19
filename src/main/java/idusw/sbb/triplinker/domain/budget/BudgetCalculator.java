package idusw.sbb.triplinker.domain.budget;

import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Item;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 예산 산식 — 외부 호출 없는 순수 함수. 같은 입력이면 늘 같은 범위가 나온다.
 *
 *   숙박      1실 1박 범위 × 방 수(2인 1실) × 박 수. 1실 1박은 관광공사 공개 요금 → 같은 지역 표본. 둘 다 없으면 미산정.
 *   식비      백반 1끼 ~ 그 2배(관광지 식당 가정) × 끼니 × 인원.
 *   현지 이동 자차 = 일정의 카카오 거리 ÷ 연비 9~14km/L × 유가, 대중교통 = 탈것 구간 × 인원 × 기본요금 ~ 일정 운임.
 *             모든 구간이 도보면 0원. 거리 · 운임이 없으면 미산정.
 *   오가는 교통 출발지와 여행지 시도가 다르면 거리 × 공개 운임 단가의 「추정」 줄이 따로 보인다(IntercityFare, 키 불필요).
 *   입장료    ① 관광공사 이용요금 → ② 조사한 공개 요금(PriceResearch 표) → ③ 같은 유형 조사값 범위(조사 추정) × 인원.
 *             셋 다 없으면 미산정.
 * 근거 없는 값을 0원이나 임의 범위로 메우지 않는다. 미산정은 합계에서 빠지고 개수로 알린다.
 * 사용자 예산은 계산에 넣지 않는다 — 결과와 비교만 한다(예전 AI 프롬프트는 합계를 예산의 90~100% 에 맞췄다).
 */
public final class BudgetCalculator {

    private BudgetCalculator() {}

    /** 1실 1박 요금 범위와 근거. */
    public record LodgingQuote(long minPerRoom, long maxPerRoom, Source source,
                               String basis, String sourceName, String asOf) {}

    /** 관광지 1곳 1인 이용요금 범위. */
    public record Admission(String name, long min, long max, Source source, String basis,
                            String sourceName, String asOf) {
        public Admission(String name, long min, long max, Source source, String basis) {
            this(name, min, max, source, basis, "한국관광공사 TourAPI 이용요금", LocalDate.now() + " 조회");
        }
    }

    /**
     * @param sido          여행지 시도(짧은 이름, 모르면 null)
     * @param departureSido 출발지 시도(모르면 null)
     * @param nights        박 수(당일치기 0)
     * @param days          일 수
     * @param people        인원
     * @param car           자차면 true, 대중교통이면 false
     * @param lodging       숙박 요금(못 찾았으면 null → 미산정)
     * @param admissions    요금을 찾은 관광지
     * @param unknownTours  요금을 못 찾은 관광지 수
     * @param transitKm     일정의 이동 거리 합(없으면 0)
     * @param transitFare   일정에 적힌 이동 금액 합(대중교통, 없으면 0)
     * @param legs          이동 구간 수
     * @param userBudget    사용자가 정한 예산(비교용)
     * @param tourApi       관광공사 키가 켜져 있었는지
     * @param walkLegs      이동 구간 중 도보(운임 0원) 수
     */
    public record Input(String sido, String departureSido, int nights, int days, int people, boolean car,
                        LodgingQuote lodging, List<Admission> admissions, int unknownTours,
                        double transitKm, long transitFare, int legs, Long userBudget, boolean tourApi, int walkLegs) {
        public Input(String sido, String departureSido, int nights, int days, int people, boolean car,
                     LodgingQuote lodging, List<Admission> admissions, int unknownTours,
                     double transitKm, long transitFare, int legs, Long userBudget, boolean tourApi) {
            this(sido, departureSido, nights, days, people, car, lodging, admissions, unknownTours,
                    transitKm, transitFare, legs, userBudget, tourApi, 0);
        }
    }

    public static BudgetEstimate compute(Input in) {
        int people = Math.max(1, in.people());
        int days = Math.max(1, in.days());
        List<Item> items = new ArrayList<>();

        // ── 숙박 ──
        if (in.nights() > 0) {
            int rooms = rooms(people);
            String label = in.nights() + "박 숙박 · 방 " + rooms + "개";
            LodgingQuote q = in.lodging();
            if (q == null) {
                items.add(missing("STAY", label, in.tourApi()
                        ? "관광공사에 이 숙소 · 같은 지역 숙소의 공개 요금이 없음"
                        : "관광공사 자료를 연결하지 않아 숙박 요금을 넣지 않음"));
            } else {
                items.add(new Item("STAY", label,
                        q.minPerRoom() * rooms * in.nights(), q.maxPerRoom() * rooms * in.nights(),
                        "1실 1박 " + won(q.minPerRoom()) + "~" + won(q.maxPerRoom()) + " × 방 " + rooms + "개(2인 1실) × "
                                + in.nights() + "박 · " + q.basis(),
                        q.source(), q.sourceName(), q.asOf()));
            }
        }

        // ── 식비 ──
        int meals = mealCount(days);
        boolean known = in.sido() != null && PublicPrices.MEAL_BY_SIDO.containsKey(in.sido());   // Map.of 는 null 키에 NPE
        long mealLo = known ? PublicPrices.MEAL_BY_SIDO.get(in.sido()) : Collections.min(PublicPrices.MEAL_BY_SIDO.values());
        long mealHi = Math.round((known ? PublicPrices.MEAL_BY_SIDO.get(in.sido()) : Collections.max(PublicPrices.MEAL_BY_SIDO.values()))
                * PublicPrices.MEAL_TOURIST_FACTOR);
        items.add(new Item("FOOD", meals + "끼 식비",
                mealLo * meals * people, mealHi * meals * people,
                "1끼 " + won(mealLo) + "~" + won(mealHi) + " × " + meals + "끼 × " + people + "명 · 백반 "
                        + (known ? in.sido() + " 평균" : "시도 평균 " + PublicPrices.MEAL_BY_SIDO.size() + "곳의 범위")
                        + ", 최대는 관광지 식당 2배 가정" + (days > 1 ? " · 첫날 아침 · 마지막 날 저녁 제외" : ""),
                Source.ASSUMED, PublicPrices.MEAL_SOURCE, PublicPrices.MEAL_AS_OF));

        // ── 오가는 교통 ──
        Item trip = IntercityFare.quote(in.departureSido(), in.sido(), people, in.car());
        if (trip != null) items.add(trip);

        // ── 현지 이동 ──
        if (in.car()) {
            if (in.transitKm() > 0) {
                double km = in.transitKm();
                items.add(new Item("TRANSPORT", "현지 이동 유류비",
                        Math.round(km / PublicPrices.KM_PER_LITER_GOOD * PublicPrices.FUEL_PER_LITER),
                        Math.round(km / PublicPrices.KM_PER_LITER_BAD * PublicPrices.FUEL_PER_LITER),
                        fmtKm(km) + "km(일정의 카카오 길찾기 거리) ÷ 연비 9~14km/L(가정) × " + won(PublicPrices.FUEL_PER_LITER) + "/L",
                        Source.ASSUMED, PublicPrices.FUEL_SOURCE, PublicPrices.FUEL_AS_OF));
            } else {
                items.add(missing("TRANSPORT", "현지 이동 유류비", "일정에 이동 거리가 없음"));
            }
        } else {
            int walk = Math.max(0, in.walkLegs());
            int rides = in.legs() - walk;   // 도보 구간은 운임이 없으니 기본요금 하한에서 뺀다
            if (in.transitFare() > 0 && rides > 0) {
                long base = PublicPrices.TRANSIT_BASE_FARE * rides * people;
                items.add(new Item("TRANSPORT", "현지 대중교통",
                        Math.min(base, in.transitFare()), Math.max(base, in.transitFare()),
                        rides + "구간" + (walk > 0 ? "(도보 " + walk + "구간 제외)" : "") + " × " + people + "명 × 기본요금 " + won(PublicPrices.TRANSIT_BASE_FARE)
                                + " ~ 일정의 거리 운임 합(카카오 거리 × 기본 1,500원 + 10km 넘는 km 당 100원, 가정)",
                        Source.ASSUMED, "카카오 길찾기 거리 · 운임식 가정", "-"));
            } else if (walk > 0 && rides == 0) {
                items.add(new Item("TRANSPORT", "현지 이동 · 도보", 0, 0,
                        "일정의 " + walk + "구간이 모두 도보(" + fmtKm(in.transitKm()) + "km) · 운임 없음",
                        Source.ASSUMED, "일정의 이동 구간", "-"));
            } else {
                items.add(missing("TRANSPORT", "현지 대중교통", "일정에 이동 구간 · 운임이 없음"));
            }
        }

        // ── 입장료 ──
        // ① 관광공사 → ② 조사한 공개 요금 → ③ 같은 유형 조사값 범위. 출처가 달라 줄을 나눈다.
        List<Admission> all = in.admissions() == null ? List.of() : in.admissions();
        List<Admission> pub = all.stream().filter(a -> a.source() == Source.PUBLIC_RATE).toList();
        if (!pub.isEmpty()) {
            long free = pub.stream().filter(a -> a.max() == 0).count();
            items.add(new Item("TOUR", "입장료 · 요금 확인 " + pub.size() + "곳",
                    pub.stream().mapToLong(Admission::min).sum() * people,
                    pub.stream().mapToLong(Admission::max).sum() * people,
                    pub.size() + "곳(무료 " + free + "곳) × " + people + "명 · 예: " + pub.get(0).basis(),
                    Source.PUBLIC_RATE, "한국관광공사 TourAPI 이용요금", LocalDate.now() + " 조회"));
        }
        tourGroup(items, all, Source.RESEARCHED, "입장료 · 요금 조사 ", people);
        tourGroup(items, all, Source.RESEARCH_ESTIMATE, "입장료 · 유형별 추정 ", people);
        if (in.unknownTours() > 0) {
            items.add(missing("TOUR", "입장료 · 요금 모름 " + in.unknownTours() + "곳",
                    in.tourApi() ? "관광공사 · 조사한 요금표에 금액이 없고, 같은 유형 조사값도 없음" : "관광공사 자료를 연결하지 않았고, 조사한 요금표에도 없음"));
        }

        List<Item> counted = items.stream().filter(i -> i.source() != Source.NOT_ESTIMATED).toList();
        long min = counted.stream().mapToLong(Item::min).sum();
        long max = counted.stream().mapToLong(Item::max).sum();
        double midAll = counted.stream().mapToDouble(i -> (i.min() + i.max()) / 2.0).sum();
        double midLive = counted.stream().filter(i -> i.source().live()).mapToDouble(i -> (i.min() + i.max()) / 2.0).sum();
        // 내림 — 99.6% 를 100% 로 올리면 「전부 공공 자료」 라는 거짓말이 된다
        int share = midAll <= 0 ? 0 : (int) Math.floor(midLive * 100 / midAll);
        int missing = items.size() - counted.size();
        String confidence = share >= 70 && missing == 0 ? "높음" : share >= 40 ? "보통" : "낮음";

        List<String> notes = new ArrayList<>();
        if (!in.tourApi()) notes.add("관광공사 자료를 연결하지 않아 숙박 · 관광공사 이용요금은 넣지 않았습니다.");
        if (all.stream().anyMatch(a -> a.source() == Source.RESEARCHED || a.source() == Source.RESEARCH_ESTIMATE))
            notes.add("「조사한 공개 요금」 은 관광공사에 요금이 없어 공식 누리집 · 지자체 안내에서 읽은 금액이고, 「조사 추정」 은 같은 유형 조사값의 범위입니다.");
        if (missing > 0) notes.add("미산정 " + missing + "개는 합계에 넣지 않았습니다. 합계는 계산한 항목만의 범위입니다.");
        if (trip != null) notes.add("출발지 ↔ 여행지 교통은 거리 × 공개 운임 단가로 낸 추정입니다. 실제 운임(TAGO 조회)은 키가 없어 연결하지 않았습니다.");
        notes.add("실제 예약 요금과 다를 수 있습니다. 숙박은 관광공사에 공개된 객실 최저요금 기준이며 예약 확정가가 아닙니다.");

        List<String> excluded = List.of("현지 통행료 · 주차비", "택시 · 렌터카", "카페 · 간식 · 쇼핑 · 기념품");

        return new BudgetEstimate(items, min, max, share, confidence, missing,
                in.userBudget(), budgetCheck(in.userBudget(), min, max, missing), excluded, notes, in.tourApi());
    }

    /** 조사값 줄 — 장소마다 금액, 출처 기관 · 조회일은 겹치지 않게 모아 적는다. */
    private static void tourGroup(List<Item> items, List<Admission> all, Source source, String label, int people) {
        List<Admission> g = all.stream().filter(a -> a.source() == source).toList();
        if (g.isEmpty()) return;
        items.add(new Item("TOUR", label + g.size() + "곳",
                g.stream().mapToLong(Admission::min).sum() * people,
                g.stream().mapToLong(Admission::max).sum() * people,
                g.size() + "곳 × " + people + "명 · " + String.join(", ", g.stream().map(Admission::basis).toList()),
                source,
                String.join(" · ", g.stream().map(Admission::sourceName).distinct().toList()),
                String.join(" · ", g.stream().map(Admission::asOf).distinct().toList())));
    }

    private static Item missing(String category, String label, String why) {
        return new Item(category, label, 0, 0, why, Source.NOT_ESTIMATED, "-", "-");
    }

    /** 2인 1실. 3명이면 2실. */
    static int rooms(int people) {
        return Math.max(1, (people + 1) / 2);
    }

    /** 끼니 — 당일치기 2끼, 그 밖엔 하루 3끼에서 첫날 아침 · 마지막 날 저녁을 뺀다. */
    static int mealCount(int days) {
        return days <= 1 ? 2 : days * 3 - 2;
    }

    static String budgetCheck(Long budget, long min, long max, int missing) {
        if (budget == null || budget <= 0) return "정한 예산이 없어 비교하지 않았습니다.";
        String tail = missing > 0 ? " (미산정 " + missing + "개 제외)" : "";
        if (budget < min) return "정한 예산 " + won(budget) + "이 예상 최소보다 " + won(min - budget) + " 적습니다." + tail;
        if (budget > max) return "정한 예산 " + won(budget) + "이 예상 최대보다 " + won(budget - max) + " 많습니다." + tail;
        return "정한 예산 " + won(budget) + "이 예상 범위 안에 있습니다." + tail;
    }

    static String won(long v) {
        return String.format("%,d원", v);
    }

    private static String fmtKm(double km) {
        return km == Math.rint(km) ? String.valueOf((long) km) : String.format("%.1f", km);
    }
}
