package idusw.sbb.triplinker.domain.budget;

import java.util.Map;

/**
 * 여행지 글(「제주 제주시」 · 「부산광역시 해운대구」) → 시도 짧은 이름 → 관광공사 areaCode.
 * 코드 표는 checkin 저장소 AreaCode.java(관광공사 API 에 직접 물어 확인한 17개)에서 옮겼다.
 */
public final class RegionCode {

    private RegionCode() {}

    private static final Map<String, String> AREA = Map.ofEntries(
            Map.entry("서울", "1"), Map.entry("인천", "2"), Map.entry("대전", "3"),
            Map.entry("대구", "4"), Map.entry("광주", "5"), Map.entry("부산", "6"),
            Map.entry("울산", "7"), Map.entry("세종", "8"), Map.entry("경기", "31"),
            Map.entry("강원", "32"), Map.entry("충북", "33"), Map.entry("충남", "34"),
            Map.entry("경북", "35"), Map.entry("경남", "36"), Map.entry("전북", "37"),
            Map.entry("전남", "38"), Map.entry("제주", "39"));

    /** 긴 이름 → 짧은 이름. 앞 두 글자로 안 되는 것만. */
    private static final Map<String, String> LONG = Map.of(
            "충청북", "충북", "충청남", "충남", "경상북", "경북", "경상남", "경남",
            "전라북", "전북", "전북특", "전북", "전라남", "전남");

    /** 시도 짧은 이름. 모르면 null. */
    public static String sido(String destination) {
        if (destination == null || destination.isBlank()) return null;
        String first = destination.trim().split("\\s+")[0];
        if (first.length() >= 3 && LONG.containsKey(first.substring(0, 3))) return LONG.get(first.substring(0, 3));
        String two = first.length() >= 2 ? first.substring(0, 2) : first;
        return AREA.containsKey(two) ? two : null;
    }

    /** 관광공사 areaCode. 모르면 null(전국에서 찾는다). */
    public static String areaCode(String sido) {
        return sido == null ? null : AREA.get(sido);
    }
}
