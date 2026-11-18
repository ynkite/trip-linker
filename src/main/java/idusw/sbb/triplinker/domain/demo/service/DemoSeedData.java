package idusw.sbb.triplinker.domain.demo.service;

import java.time.LocalDate;
import java.util.List;

/**
 * 테스트 유저에게 넣는 예시 데이터(고정값).
 *
 * AI를 부르지 않는다. 장소는 저장소의 기존 예시(DataInitializer, AiRouteService 예시)와
 * 공공 관광지 이름만 쓴다. 좌표는 지도에 핀과 동선을 그리기 위한 대략값이며,
 * 이름은 카카오 장소 검색으로 찾을 수 있는 실제 이름이다.
 *
 * 날짜는 되돌릴 때의 오늘을 기준으로 잡아 지난 여행 둘, 다가오는 여행 둘이 항상 섞여 보이게 한다.
 * 여행지에 '해운대'를 넣지 않는다(고정 경로 모드의 원본 탐색 키워드와 겹치지 않게).
 */
final class DemoSeedData {

    private DemoSeedData() { }

    /** type: stay / food / cafe / tour (AiRouteService 와 같은 네 가지) */
    record Stop(String type, String name, double lat, double lng, String time, long amount) { }

    record Day(List<Stop> stops) { }

    record Trip(String key, String title, String destination, int startOffsetDays,
                String transport, String companion, int companionCount, long budget,
                String styles, List<Day> days) {

        LocalDate startDate(LocalDate today) { return today.plusDays(startOffsetDays); }

        LocalDate endDate(LocalDate today) { return startDate(today).plusDays(days.size() - 1L); }

        boolean isPast(LocalDate today) { return endDate(today).isBefore(today); }
    }

    record Review(String tripKey, String title, String content, String styleTags, String category) { }

    static final List<Trip> TRIPS = List.of(
            new Trip("seoul", "서울 고궁 산책 1박 2일", "서울", -40,
                    "🚌 대중교통", "친구", 2, 500_000L, "문화·역사,도심",
                    List.of(
                            new Day(List.of(
                                    new Stop("tour", "경복궁", 37.5796, 126.9770, "10:00", 3_000L),
                                    new Stop("tour", "북촌한옥마을", 37.5826, 126.9831, "13:00", 0L),
                                    new Stop("food", "광장시장", 37.5701, 126.9997, "18:00", 20_000L),
                                    new Stop("stay", "롯데호텔 서울", 37.5651, 126.9810, "21:00", 210_000L))),
                            new Day(List.of(
                                    new Stop("tour", "창덕궁", 37.5794, 126.9910, "09:30", 3_000L),
                                    new Stop("tour", "인사동 쌈지길", 37.5744, 126.9852, "12:30", 0L),
                                    new Stop("tour", "N서울타워", 37.5512, 126.9882, "16:00", 21_000L))))),

            new Trip("jeju", "제주 서쪽 해안 2박 3일", "제주", -20,
                    "🚗 자차", "커플", 2, 900_000L, "자연,오션뷰,힐링",
                    List.of(
                            new Day(List.of(
                                    new Stop("food", "협재수우동", 33.3933, 126.2412, "12:30", 15_000L),
                                    new Stop("tour", "협재해수욕장", 33.3940, 126.2396, "14:00", 0L),
                                    new Stop("tour", "한림공원", 33.3894, 126.2390, "16:00", 12_000L),
                                    new Stop("stay", "제주신라호텔", 33.2477, 126.4081, "19:30", 180_000L))),
                            new Day(List.of(
                                    new Stop("cafe", "오설록 티뮤지엄", 33.3059, 126.2895, "10:00", 8_000L),
                                    new Stop("tour", "수월봉", 33.2956, 126.1628, "13:00", 0L),
                                    new Stop("tour", "송악산", 33.1995, 126.2915, "16:00", 0L),
                                    new Stop("stay", "제주신라호텔", 33.2477, 126.4081, "19:30", 180_000L))),
                            new Day(List.of(
                                    new Stop("tour", "성산일출봉", 33.4589, 126.9425, "10:00", 5_000L),
                                    new Stop("tour", "섭지코지", 33.4242, 126.9307, "13:00", 0L))))),

            new Trip("gyeongju", "경주 역사 기행 2박 3일", "경주", 14,
                    "🚗 자차", "가족", 4, 800_000L, "문화·역사,가족,힐링",
                    List.of(
                            new Day(List.of(
                                    new Stop("tour", "대릉원", 35.8383, 129.2112, "14:00", 3_000L),
                                    new Stop("tour", "첨성대", 35.8347, 129.2190, "15:30", 0L),
                                    new Stop("tour", "동궁과 월지", 35.8349, 129.2265, "19:30", 3_000L),
                                    new Stop("stay", "라궁 한옥호텔", 35.8417, 129.2862, "21:30", 150_000L))),
                            new Day(List.of(
                                    new Stop("tour", "불국사", 35.7901, 129.3321, "09:00", 6_000L),
                                    new Stop("tour", "석굴암", 35.7950, 129.3490, "11:00", 6_000L),
                                    new Stop("food", "경주 교리김밥", 35.8299, 129.2155, "13:30", 8_000L),
                                    new Stop("tour", "국립경주박물관", 35.8296, 129.2282, "16:00", 0L),
                                    new Stop("stay", "라궁 한옥호텔", 35.8417, 129.2862, "20:00", 150_000L))),
                            new Day(List.of(
                                    new Stop("tour", "황리단길", 35.8378, 129.2098, "10:00", 0L))))),

            new Trip("jeonju", "전주 한옥마을 당일 나들이", "전주", 30,
                    "🚌 대중교통", "혼자", 1, 100_000L, "문화·역사,음식 탐방",
                    List.of(
                            new Day(List.of(
                                    new Stop("tour", "경기전", 35.8153, 127.1497, "10:00", 3_000L),
                                    new Stop("tour", "전동성당", 35.8134, 127.1491, "11:00", 0L),
                                    new Stop("food", "전주남부시장", 35.8125, 127.1465, "12:30", 12_000L),
                                    new Stop("tour", "전주한옥마을", 35.8150, 127.1530, "14:00", 0L),
                                    new Stop("tour", "오목대", 35.8133, 127.1548, "16:00", 0L)))))
    );

    /** 가계부에 실제 쓴 돈으로 더 넣는 줄(지난 여행만). {tripKey, dayIndex, category, description, amount} */
    static final Object[][] EXTRA_EXPENSES = {
            {"seoul", 0, "CAFE", "북촌 골목 카페", 11_000L},
            {"seoul", 1, "FOOD", "인사동 점심", 24_000L},
            {"jeju",  0, "CAFE", "협재 바닷가 카페", 13_000L},
            {"jeju",  1, "FOOD", "송악산 근처 저녁", 46_000L},
            {"jeju",  2, "FOOD", "성산 근처 점심", 38_000L},
    };

    static final List<Review> REVIEWS = List.of(
            new Review("seoul", "서울 고궁 산책 1박 2일 다녀왔어요",
                    "경복궁에서 시작해 북촌한옥마을까지 걸어서 이동했습니다. 오후 햇빛이 좋아서 한옥 골목 사진이 잘 나왔어요.\n"
                            + "저녁은 광장시장에서 빈대떡과 육회를 먹었고, 둘째 날은 창덕궁과 인사동을 거쳐 N서울타워에서 마무리했습니다.\n"
                            + "대중교통만으로 충분히 다닐 수 있는 동선이라 처음 서울 오는 친구와 다니기 좋았습니다.",
                    "문화·역사,도심,힐링", "ROUTE"),
            new Review("jeju", "제주 서쪽 해안 2박 3일 후기",
                    "협재해수욕장 물빛이 정말 맑았고 바로 앞 한림공원도 같이 둘러보기 좋았습니다.\n"
                            + "둘째 날은 오설록 티뮤지엄에서 쉬었다가 수월봉과 송악산 해안길을 걸었어요. 바람이 세니 겉옷 꼭 챙기세요.\n"
                            + "마지막 날은 동쪽으로 넘어가 성산일출봉과 섭지코지를 보고 공항으로 갔습니다.",
                    "자연,오션뷰,힐링", "ROUTE"),
            new Review("jeju", "오설록 티뮤지엄 들렀다 온 이야기",
                    "녹차밭 사이 산책로가 짧아서 일정 중간에 잠깐 들르기 좋습니다. 녹차 아이스크림은 줄이 길어도 먹어볼 만했어요.",
                    "힐링,음식 탐방", "CAFE")
    );
}
