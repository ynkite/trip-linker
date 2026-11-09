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
 *   현지 이동 자차 = 일정의 카카오 거리 ÷ 연비 9~14km/L × 유가, 대중교통 = 구간 × 인원 × 기본요금 ~ 일정 운임. 거리 · 운임이 없으면 미산정.
 *   오가는 교통 출발지와 여행지 시도가 다르면 「미산정」 줄로 따로 보인다(운임 자료 미연결).
 *   입장료    관광공사 이용요금을 찾은 곳만 그 값 × 인원. 못 찾은 곳은 미산정.
 * 근거 없는 값을 0원이나 임의 범위로 메우지 않는다. 미산정은 합계에서 빠지고 개수로 알린다.
 * 사용자 예산은 계산에 넣지 않는다 — 결과와 비교만 한다(예전 AI 프롬프트는 합계를 예산의 90~100% 에 맞췄다).
 */
public final class BudgetCalculator {

    private BudgetCalculator() {}

    /** 1실 1박 요금 범위와 근거. */
    public record LodgingQuote(long minPerRoom, long maxPerRoom, Source source,
                               String basis, String sourceName, String asOf) {}

    /** 관광지 1곳 1인 이용요금 범위. */
    public record Admission(String name, long min, long max, Source source, String basis) {}

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
     */
    public record Input(String sido, String departureSido, int nights, int days, int people, boolean car,
                        LodgingQuote lodging, List<Admission> admissions, int unknownTours,
                        double transitKm, long transitFare, int legs, Long userBudget, boolean tourApi) {}

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
                        : "관광공사 키(TOUR_API_KEY)가 없어 숙박 요금을 조회하지 않음"));
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
                        + (known ? in.sido() + " 평균" : "서울 · 부산 · 제주 평균의 범위")
                        + ", 최대는 관광지 식당 2배 가정" + (days > 1 ? " · 첫날 아침 · 마지막 날 저녁 제외" : ""),
                Source.ASSUMED, PublicPrices.MEAL_SOURCE, PublicPrices.MEAL_AS_OF));

        // ── 오가는 교통 ──
        if (in.departureSido() != null && in.sido() != null && !in.departureSido().equals(in.sido())) {
            items.add(missing("TRANSPORT", in.departureSido() + " ↔ " + in.sido() + " 왕복 교통",
                    in.car() ? "출발지 ~ 여행지 도로 거리 · 통행료 자료를 아직 연결하지 않음"
                             : "KTX · 고속버스 · 항공 운임 자료를 아직 연결하지 않음"));
        }

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
            if (in.transitFare() > 0 && in.legs() > 0) {
                long base = PublicPrices.TRANSIT_BASE_FARE * in.legs() * people;
                items.add(new Item("TRANSPORT", "현지 대중교통",
                        Math.min(base, in.transitFare()), Math.max(base, in.transitFare()),
                        in.legs() + "구간 × " + people + "명 × 기본요금 " + won(PublicPrices.TRANSIT_BASE_FARE)
                                + " ~ 일정의 거리 운임 합(카카오 거리 × 기본 1,500원 + 10km 넘는 km 당 100원, 가정)",
                        Source.ASSUMED, "카카오 길찾기 거리 · 운임식 가정", "-"));
            } else {
                items.add(missing("TRANSPORT", "현지 대중교통", "일정에 이동 구간 · 운임이 없음"));
            }
        }

        // ── 입장료 ──
        List<Admission> found = in.admissions() == null ? List.of() : in.admissions();
        if (!found.isEmpty()) {
            long free = found.stream().filter(a -> a.max() == 0).count();
            items.add(new Item("TOUR", "입장료 · 요금 확인 " + found.size() + "곳",
                    found.stream().mapToLong(Admission::min).sum() * people,
                    found.stream().mapToLong(Admission::max).sum() * people,
                    found.size() + "곳(무료 " + free + "곳) × " + people + "명 · 예: " + found.get(0).basis(),
                    Source.PUBLIC_RATE, "한국관광공사 TourAPI 이용요금", LocalDate.now() + " 조회"));
        }
        if (in.unknownTours() > 0) {
            items.add(missing("TOUR", "입장료 · 요금 모름 " + in.unknownTours() + "곳",
                    in.tourApi() ? "관광공사에 이용요금이 없거나 글에 금액이 없음" : "관광공사 키(TOUR_API_KEY)가 없어 조회하지 않음"));
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
        if (!in.tourApi()) notes.add("관광공사 키(TOUR_API_KEY)가 없어 숙박 · 입장료를 조회하지 않았습니다.");
        if (missing > 0) notes.add("미산정 " + missing + "개는 합계에 넣지 않았습니다. 합계는 계산한 항목만의 범위입니다.");
        notes.add("실제 예약 요금과 다를 수 있습니다. 숙박은 관광공사에 공개된 객실 최저요금 기준이며 예약 확정가가 아닙니다.");

        List<String> excluded = List.of("고속도로 통행료 · 주차비", "택시 · 렌터카", "카페 · 간식 · 쇼핑 · 기념품");

        return new BudgetEstimate(items, min, max, share, confidence, missing,
                in.userBudget(), budgetCheck(in.userBudget(), min, max, missing), excluded, notes, in.tourApi());
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
