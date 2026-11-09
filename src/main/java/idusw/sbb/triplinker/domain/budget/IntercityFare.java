package idusw.sbb.triplinker.domain.budget;

import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Item;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;

import java.util.Map;

/**
 * 출발지 ↔ 여행지 왕복 교통비 — 키 없이 거리 × 공개 운임 단가로 낸 「추정」.
 * 시도청 좌표의 직선거리 × 도로 계수 = 도로 거리. 대중교통은 고속버스 일반 ~ KTX 단가, 제주는 항공, 자차는 유류비 ~ 유류비 + 통행료.
 * 단가는 PublicPrices(원문 미확인)라 상태는 늘 「가정」이고 항목 이름에 「추정」 을 붙인다. TAGO 실조회는 키(TAGO_API_KEY)가 생긴 뒤.
 */
public final class IntercityFare {

    private IntercityFare() {}

    /** 시도청 위도 · 경도. */
    private static final Map<String, double[]> POS = Map.ofEntries(
            Map.entry("서울", new double[]{37.566, 126.978}), Map.entry("인천", new double[]{37.456, 126.705}),
            Map.entry("대전", new double[]{36.351, 127.385}), Map.entry("대구", new double[]{35.871, 128.601}),
            Map.entry("광주", new double[]{35.160, 126.851}), Map.entry("부산", new double[]{35.180, 129.075}),
            Map.entry("울산", new double[]{35.538, 129.311}), Map.entry("세종", new double[]{36.480, 127.289}),
            Map.entry("경기", new double[]{37.263, 127.029}), Map.entry("강원", new double[]{37.885, 127.730}),
            Map.entry("충북", new double[]{36.642, 127.489}), Map.entry("충남", new double[]{36.601, 126.660}),
            Map.entry("경북", new double[]{36.568, 128.729}), Map.entry("경남", new double[]{35.228, 128.681}),
            Map.entry("전북", new double[]{35.824, 127.148}), Map.entry("전남", new double[]{34.816, 126.463}),
            Map.entry("제주", new double[]{33.489, 126.498}));

    /** 두 시도청 사이 도로 거리(km). 모르는 시도면 -1. */
    public static double roadKm(String a, String b) {
        double[] p = POS.get(a), q = POS.get(b);
        if (p == null || q == null) return -1;
        double r = 6371, dLat = Math.toRadians(q[0] - p[0]), dLon = Math.toRadians(q[1] - p[1]);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(p[0])) * Math.cos(Math.toRadians(q[0])) * Math.pow(Math.sin(dLon / 2), 2);
        return Math.round(2 * r * Math.asin(Math.sqrt(h)) * PublicPrices.ROAD_FACTOR);
    }

    /** 왕복 교통 항목. 시도가 같거나 모르면 null(줄 없음). 거리를 못 구하면 호출 쪽이 미산정으로 둔다. */
    public static Item quote(String from, String to, int people, boolean car) {
        if (from == null || to == null || from.equals(to)) return null;
        double km = roadKm(from, to);
        if (km <= 0) return null;
        String label = from + " ↔ " + to + " 왕복 교통(추정)";
        boolean island = "제주".equals(from) || "제주".equals(to);
        String src = PublicPrices.FARE_SOURCE, asOf = PublicPrices.FARE_AS_OF;
        if (island) {
            return new Item("TRANSPORT", label, PublicPrices.FLIGHT_MIN * people * 2, PublicPrices.FLIGHT_MAX * people * 2,
                    "추정 · 항공 편도 1인 " + BudgetCalculator.won(PublicPrices.FLIGHT_MIN) + "~" + BudgetCalculator.won(PublicPrices.FLIGHT_MAX)
                            + " × " + people + "명 × 2(왕복) · 섬이라 항공 가정(렌터카 · 현지 교통 별도)",
                    Source.ASSUMED, src, asOf);
        }
        String k = BudgetCalculator.won(Math.round(km)).replace("원", "km");
        if (car) {
            long fuelLo = Math.round(km * 2 / PublicPrices.KM_PER_LITER_GOOD * PublicPrices.FUEL_PER_LITER);
            long fuelHi = Math.round(km * 2 / PublicPrices.KM_PER_LITER_BAD * PublicPrices.FUEL_PER_LITER);
            long toll = Math.round(km * 2 * PublicPrices.TOLL_PER_KM);
            return new Item("TRANSPORT", label, fuelLo, fuelHi + toll,
                    "추정 · 도로 약 " + k + " × 2(왕복) ÷ 연비 9~14km/L × " + BudgetCalculator.won(PublicPrices.FUEL_PER_LITER)
                            + "/L, 최대는 통행료 km당 " + PublicPrices.TOLL_PER_KM + "원 포함",
                    Source.ASSUMED, src, asOf);
        }
        long lo = Math.max(PublicPrices.FARE_FLOOR, Math.round(km * PublicPrices.BUS_PER_KM));
        long hi = Math.max(lo, Math.round(km * PublicPrices.KTX_PER_KM));
        return new Item("TRANSPORT", label, lo * people * 2, hi * people * 2,
                "추정 · 도로 약 " + k + " × 1인 편도 " + BudgetCalculator.won(lo) + "(고속버스 일반 단가)~" + BudgetCalculator.won(hi)
                        + "(KTX 단가) × " + people + "명 × 2(왕복)",
                Source.ASSUMED, src, asOf);
    }
}
