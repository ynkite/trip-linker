package idusw.sbb.triplinker.domain.budget;

import java.util.Map;

/**
 * 공공 통계 단가 — 출처 · 기준일을 값 옆에 둔다. 값이 바뀌면 이 파일만 고친다.
 *
 * 2026-11-09 웹 조회값(크롬 ChatGPT · 제미나이 답)이고 원문 표는 직접 열어 보지 못했다.
 * 그래서 이 값으로 낸 항목은 화면에 「가정」 으로 나간다(회의 판결: 원문 미확인 값은 확정 기준값으로 쓰지 않는다).
 * 원문을 확인하면 출처 이름 끝의 「원문 미확인」 을 지우고 상태를 바꾼다.
 */
public final class PublicPrices {

    private PublicPrices() {}

    /** 오피넷 전국 보통휘발유 평균 판매가(원/L). 두 웹 답이 같은 값. */
    public static final long FUEL_PER_LITER = 1_855;
    public static final String FUEL_SOURCE = "오피넷 전국 보통휘발유 평균(웹 조회값 · 원문 미확인)";
    public static final String FUEL_AS_OF = "2026-11-08";

    /** 승용차 연비 범위(km/L) — 가정. 좋은 쪽이 최소 비용, 나쁜 쪽이 최대 비용. */
    public static final double KM_PER_LITER_GOOD = 14.0;
    public static final double KM_PER_LITER_BAD = 9.0;

    /** 참가격 외식비 「김치찌개백반」 시도 평균(원). 웹 답이 엇갈려(서울 8,769 · 8,654) 세 시도 값을 모두 준 쪽을 쓴다. */
    public static final Map<String, Long> MEAL_BY_SIDO = Map.of(
            "서울", 8_654L,
            "부산", 8_429L,
            "제주", 9_625L);
    public static final String MEAL_SOURCE = "한국소비자원 참가격 외식비 김치찌개백반(웹 조회값 · 원문 미확인)";
    public static final String MEAL_AS_OF = "2026-11-09 조회";

    /** 관광지 식당은 시내 백반보다 비싸다 — 1끼 최대 = 백반 × 2 (가정). */
    public static final double MEAL_TOURIST_FACTOR = 2.0;

    /** 대중교통 1구간 1인 기본요금(원) — 일정 생성 코드의 운임식(기본 1,500 + 10km 넘는 km 당 100)과 같은 가정. */
    public static final long TRANSIT_BASE_FARE = 1_500;

    /** 시도 간 도로 거리 = 시도청 직선거리 × 이 계수(가정). */
    public static final double ROAD_FACTOR = 1.2;
    /** 고속버스 일반 · KTX 일반실 1km 당 운임(원) — 서울~부산 대표 운임을 도로 거리로 나눈 값(가정, 원문 미확인). */
    public static final long BUS_PER_KM = 60;
    public static final long KTX_PER_KM = 150;
    /** 가까운 시도 사이에도 편도 1인 최소 운임(원). */
    public static final long FARE_FLOOR = 3_000;
    /** 제주 ↔ 육지 항공 편도 1인 범위(원) — 저가 ~ 일반 운임 대표값(가정, 원문 미확인). */
    public static final long FLIGHT_MIN = 30_000;
    public static final long FLIGHT_MAX = 100_000;
    /** 승용차 고속도로 통행료 1km 당(원) — 가정, 원문 미확인. */
    public static final long TOLL_PER_KM = 45;
    public static final String FARE_SOURCE = "공개 운임표 대표 구간 환산 단가(TAGO 미조회 · 원문 미확인)";
    public static final String FARE_AS_OF = "2026-11-09";
}
