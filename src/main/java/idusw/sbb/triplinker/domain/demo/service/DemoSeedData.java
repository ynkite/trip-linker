package idusw.sbb.triplinker.domain.demo.service;

import java.time.LocalDate;
import java.util.List;

/**
 * 테스트 유저에게 넣는 예시 데이터(고정값).
 *
 * AI를 부르지 않는다. 장소 이름과 좌표는 카카오 장소 검색 결과 그대로다(2026-11 확인).
 * 하루 동선은 한 권역 안에서 한 방향이나 작은 고리로 이어지게 짰고,
 * 장소를 바꾸면 DemoSeedRouteTest 가 이웃 거리·순서·선 엇갈림을 다시 검사한다.
 *
 * 날짜는 되돌릴 때의 오늘을 기준으로 잡아 지난 여행 둘, 다가오는 여행 둘이 항상 섞여 보이게 한다.
 * 여행지(destination)에 '해운대'를 넣지 않는다(고정 경로 모드의 원본 탐색 키워드와 겹치지 않게).
 */
final class DemoSeedData {

    private DemoSeedData() { }

    /** type: stay / food / cafe / tour (AiRouteService 와 같은 네 가지) */
    record Stop(String type, String name, double lat, double lng, String time, long amount) { }

    record Day(List<Stop> stops) { }

    /** longDrive: 차로 권역을 넓게 도는 일정(강원). 동선 검사 기준을 조금 넓게 본다. */
    record Trip(String key, String title, String destination, int startOffsetDays,
                String transport, String companion, int companionCount, long budget,
                String styles, boolean longDrive, List<Day> days) {

        LocalDate startDate(LocalDate today) { return today.plusDays(startOffsetDays); }

        LocalDate endDate(LocalDate today) { return startDate(today).plusDays(days.size() - 1L); }

        boolean isPast(LocalDate today) { return endDate(today).isBefore(today); }
    }

    /** 테스트 유저가 쓴 글. 같은 여행의 장소 별점(USER 몫)은 ROUTE 글에 붙는다. */
    record Review(String tripKey, String title, String content, String styleTags, String category) { }

    /** 후기 전용 테스트 계정. 로그인할 수 없고, 이름에 테스트 계정임이 드러난다. */
    record Reviewer(String key, String username, String name, String email) { }

    /**
     * 장소 하나에 남기는 별점과 한두 문장 소감.
     * reviewer 가 USER 면 테스트 유저(지난 여행에만), 아니면 후기 전용 계정.
     * daysAgo 는 후기 전용 계정 글의 작성일(오늘 기준 며칠 전). 테스트 유저 몫은 여행 직후로 잡는다.
     */
    record Rating(String reviewer, String tripKey, String place, int stars, String comment, int daysAgo) { }

    static final String USER = "USER";

    static final List<Reviewer> REVIEWERS = List.of(
            new Reviewer("A", "demo_review_a", "테스트 리뷰어 A", "demo_review_a@example.com"),
            new Reviewer("B", "demo_review_b", "테스트 리뷰어 B", "demo_review_b@example.com"));

    /** 대표 화면(캡처)으로 권하는 일정. 이 일정의 장소는 모두 평점이 보여야 한다. */
    static final String SHOWCASE_TRIP = "busan";

    static final List<Trip> TRIPS = List.of(
            new Trip("busan", "부산 해운대·광안리 1박 2일", "부산", -45,
                    "🚌 대중교통", "커플", 2, 700_000L, "오션뷰,도심,힐링", false,
                    List.of(
                            // 미포에서 서쪽으로 해변을 따라 동백섬까지, 숙소는 동백섬 입구
                            new Day(List.of(
                                    new Stop("tour", "해운대블루라인파크 미포정거장", 35.15815, 129.17281, "10:30", 14_000L),
                                    new Stop("food", "해운대암소갈비집", 35.16311, 129.16646, "12:30", 96_000L),
                                    new Stop("tour", "해운대해수욕장", 35.15852, 129.15985, "14:30", 0L),
                                    new Stop("tour", "동백섬", 35.15394, 129.15229, "16:30", 0L),
                                    new Stop("cafe", "더베이101", 35.15656, 129.15202, "18:30", 32_000L),
                                    new Stop("stay", "웨스틴조선 부산", 35.15611, 129.15402, "21:00", 280_000L))),
                            // 숙소에서 나와 수영강 건너 APEC나루공원, 남쪽으로 내려와 광안리까지
                            new Day(List.of(
                                    new Stop("stay", "웨스틴조선 부산", 35.15611, 129.15402, "09:00", 0L),
                                    new Stop("tour", "APEC나루공원", 35.16791, 129.12732, "10:00", 0L),
                                    new Stop("tour", "민락수변공원", 35.15494, 129.13400, "11:30", 0L),
                                    new Stop("food", "밀락더마켓", 35.15428, 129.12700, "12:30", 38_000L),
                                    new Stop("tour", "광안리해수욕장", 35.15319, 129.11898, "14:30", 0L))))),

            new Trip("sokcho", "속초 설악·바다 1박 2일", "속초", -75,
                    "🚗 자차", "가족", 4, 900_000L, "자연,오션뷰,가족", true,
                    List.of(
                            // 설악산에서 시내로 나와 바닷가를 따라 남쪽 대포항까지
                            new Day(List.of(
                                    new Stop("tour", "설악산 국립공원", 38.17299, 128.49253, "10:00", 60_000L),
                                    new Stop("food", "속초관광수산시장", 38.20454, 128.59025, "13:00", 60_000L),
                                    new Stop("tour", "아바이마을", 38.20218, 128.59423, "14:30", 2_000L),
                                    new Stop("tour", "속초해수욕장", 38.19005, 128.60410, "16:00", 0L),
                                    new Stop("food", "대포항", 38.17415, 128.60514, "18:30", 120_000L),
                                    new Stop("stay", "롯데리조트 속초", 38.18072, 128.61320, "20:30", 260_000L))),
                            // 숙소에서 나와 닭강정을 사서 청초호로, 북쪽 영금정·속초등대로
                            new Day(List.of(
                                    new Stop("stay", "롯데리조트 속초", 38.18072, 128.61320, "09:00", 0L),
                                    new Stop("food", "만석닭강정", 38.19028, 128.58686, "11:00", 22_000L),
                                    new Stop("tour", "청초호유원지", 38.19164, 128.58533, "12:00", 0L),
                                    new Stop("tour", "영금정", 38.21181, 128.60159, "13:30", 0L),
                                    new Stop("tour", "속초등대", 38.21364, 128.60031, "14:30", 0L))))),

            new Trip("daegu", "대구 중구 근대골목 당일", "대구", 12,
                    "🚌 대중교통", "친구", 2, 200_000L, "문화·역사,음식 탐방", false,
                    List.of(
                            // 달성공원에서 동쪽으로 걸어 김광석길, 저녁은 동인동
                            new Day(List.of(
                                    new Stop("tour", "달성공원", 35.87403, 128.57776, "10:00", 0L),
                                    new Stop("food", "서문시장", 35.86891, 128.58077, "11:30", 24_000L),
                                    new Stop("tour", "청라언덕", 35.86680, 128.58525, "13:00", 0L),
                                    new Stop("tour", "계산성당", 35.86796, 128.58781, "13:40", 0L),
                                    new Stop("tour", "김광석다시그리기길", 35.86029, 128.60695, "15:30", 0L),
                                    new Stop("food", "봉산찜갈비", 35.87123, 128.60516, "18:00", 70_000L))))),

            new Trip("daejeon", "대전 원도심 당일 나들이", "대전", 26,
                    "🚌 대중교통", "혼자", 1, 100_000L, "음식 탐방,도심", false,
                    List.of(
                            // 옛 충남도청에서 동쪽으로 대전역 뒤 소제동까지
                            new Day(List.of(
                                    new Stop("tour", "대전근현대사전시관", 36.32680, 127.42089, "10:00", 0L),
                                    new Stop("food", "광천식당", 36.32867, 127.42360, "11:30", 12_000L),
                                    new Stop("cafe", "성심당 본점", 36.32774, 127.42725, "13:00", 18_000L),
                                    new Stop("tour", "대전중앙시장", 36.32955, 127.43320, "14:30", 8_000L),
                                    new Stop("tour", "소제동 카페거리", 36.33635, 127.43642, "16:00", 7_000L)))))
    );

    /** 가계부에 실제 쓴 돈으로 더 넣는 줄(지난 여행만). {tripKey, dayIndex, category, description, amount} */
    static final Object[][] EXTRA_EXPENSES = {
            {"busan",  0, "FOOD", "해운대 간식", 9_000L},
            {"busan",  1, "CAFE", "광안리 바닷가 카페", 12_000L},
            {"sokcho", 0, "CAFE", "속초해수욕장 근처 카페", 18_000L},
            {"sokcho", 1, "FOOD", "동명항 근처 간식", 9_000L},
    };

    /** 테스트 유저가 스크랩해 둔 장소(다가오는 여행에서 갈 곳). */
    static final List<String> PLACE_SCRAPS = List.of("성심당 본점", "서문시장", "김광석다시그리기길");

    static final List<Review> REVIEWS = List.of(
            new Review("busan", "부산 해운대·광안리 1박 2일 다녀왔어요",
                    "첫날은 미포정거장에서 해변열차를 타고 바다를 보다가 점심은 해운대암소갈비집에서 먹었어요. "
                            + "오후에는 해운대해수욕장을 따라 동백섬까지 걸었고, 해 질 무렵 더베이101에서 마린시티 야경을 봤습니다.\n"
                            + "둘째 날은 APEC나루공원 강변을 산책하고 민락수변공원을 지나 밀락더마켓에서 점심, "
                            + "광안리해수욕장에서 광안대교를 보며 마무리했어요.\n"
                            + "이틀 모두 걷거나 지하철로 짧게 이동해서 동선이 편했습니다.",
                    "오션뷰,도심,힐링", "ROUTE"),
            new Review("sokcho", "속초 1박 2일, 설악산부터 영금정까지",
                    "오전에 설악산 소공원 쪽을 걷고 내려와 속초관광수산시장에서 점심을 먹었습니다. "
                            + "갯배를 타고 아바이마을을 둘러본 뒤 속초해수욕장에서 쉬었고, 저녁은 대포항 근처에서 먹었어요.\n"
                            + "다음 날은 청초호유원지를 한 바퀴 돌고 닭강정을 사 들고 영금정과 속초등대까지 올라갔다 왔습니다.\n"
                            + "아이들과 차로 다니기에 거리가 길지 않아 좋았어요.",
                    "자연,오션뷰,가족", "ROUTE"),
            new Review("busan", "동백섬 한 바퀴, 해 질 무렵이 제일 좋았어요",
                    "해운대해수욕장 끝에서 동백섬 산책로로 들어가 한 바퀴 도는 데 천천히 걸어도 오래 걸리지 않았어요. "
                            + "바다 쪽으로 난 데크길에서 광안대교와 마린시티가 같이 보여서 사진 찍기 좋았습니다.",
                    "힐링,오션뷰", "TOUR")
    );

    /**
     * 별점. 장소마다 3·4·5를 섞어 평균이 서로 다르게 했다(3점 미만 없음).
     * 이미 다른 회원 후기가 있는 장소에는 넣지 않는다(DemoSeedService 가 건너뜀). 그래야 운영의
     * 인기 장소 순위와 평균이 바뀌지 않는다. 장소당 최대 3개라 후기 수 순위 위쪽에도 오르지 않는다.
     */
    static final List<Rating> RATINGS = List.of(
            // 부산(대표 일정): 모든 장소
            new Rating(USER, "busan", "해운대블루라인파크 미포정거장", 5, "해변열차 창가 자리에 앉으니 바다가 바로 옆이라 시작부터 기분이 좋았어요.", 0),
            new Rating("A",  "busan", "해운대블루라인파크 미포정거장", 4, "주말 오전이라 대기가 조금 있었지만 탈 만했습니다.", 92),
            new Rating("B",  "busan", "해운대블루라인파크 미포정거장", 4, "청사포 쪽으로 가는 길 풍경이 예뻐요. 표는 미리 사 두는 편이 낫겠어요.", 118),

            new Rating(USER, "busan", "해운대암소갈비집", 4, "점심에 들렀는데 고기가 부드러웠어요. 기념일 식사로 다시 오고 싶어요.", 0),
            new Rating("A",  "busan", "해운대암소갈비집", 5, "부모님 모시고 갔는데 다들 만족하셨어요.", 101),
            new Rating("B",  "busan", "해운대암소갈비집", 4, "웨이팅이 있어서 일찍 가길 잘했다 싶었습니다.", 136),

            new Rating(USER, "busan", "해운대해수욕장", 4, "모래사장이 넓어서 걷기만 해도 좋았어요.", 0),
            new Rating("A",  "busan", "해운대해수욕장", 5, "아침 일찍 가면 사람이 적어서 한적하게 산책할 수 있어요.", 87),
            new Rating("B",  "busan", "해운대해수욕장", 3, "성수기라 사람이 정말 많았어요. 비수기에 다시 와 보고 싶네요.", 149),

            new Rating(USER, "busan", "동백섬", 4, "해 질 무렵 데크길이 특히 예뻤습니다.", 0),
            new Rating("A",  "busan", "동백섬", 3, "산책로는 좋은데 그늘이 적어 한낮엔 조금 더웠어요.", 110),
            new Rating("B",  "busan", "동백섬", 4, "누리마루 쪽 전망이 시원해요.", 127),

            new Rating(USER, "busan", "더베이101", 4, "마린시티 불빛이 물에 비치는 걸 보면서 쉬기 좋았어요.", 0),
            new Rating("A",  "busan", "더베이101", 5, "야경 사진은 여기서 찍은 게 제일 잘 나왔어요.", 95),
            new Rating("B",  "busan", "더베이101", 3, "저녁엔 자리가 금방 차서 조금 기다렸습니다.", 141),

            new Rating(USER, "busan", "웨스틴조선 부산", 4, "동백섬 바로 앞이라 아침 산책 나가기 편했어요.", 0),
            new Rating("A",  "busan", "웨스틴조선 부산", 3, "위치는 최고인데 객실은 조금 연식이 느껴졌어요.", 104),
            new Rating("B",  "busan", "웨스틴조선 부산", 3, "바다 쪽 방이라 창밖 보는 재미가 있었습니다.", 131),

            new Rating(USER, "busan", "APEC나루공원", 3, "강바람 맞으며 걷기 좋았지만 볼거리가 많지는 않아요.", 0),
            new Rating("A",  "busan", "APEC나루공원", 4, "센텀 쪽 고층 건물과 강이 같이 보여서 산책 코스로 괜찮았어요.", 98),
            new Rating("B",  "busan", "APEC나루공원", 4, "자전거 타는 분들이 많았어요. 벤치가 곳곳에 있어 쉬어 가기 좋아요.", 145),

            new Rating(USER, "busan", "민락수변공원", 4, "광안대교가 정면으로 보이는 자리라 한참 앉아 있었어요.", 0),
            new Rating("A",  "busan", "민락수변공원", 5, "밤에 오면 다리 조명이 켜져서 더 멋있습니다.", 89),
            new Rating("B",  "busan", "민락수변공원", 4, "계단에 앉아 바다 보기 좋은데 바람이 꽤 불어요.", 122),

            new Rating(USER, "busan", "밀락더마켓", 3, "가게가 여럿이라 고르기 좋았지만 점심시간엔 자리 잡기가 어려웠어요.", 0),
            new Rating("A",  "busan", "밀락더마켓", 4, "창가 쪽에서 광안대교가 보여요.", 113),
            new Rating("B",  "busan", "밀락더마켓", 3, "구경하는 재미는 있는데 조금 붐볐습니다.", 152),

            new Rating(USER, "busan", "광안리해수욕장", 5, "광안대교를 보면서 걷는 마지막 코스로 딱이었어요.", 0),
            new Rating("A",  "busan", "광안리해수욕장", 4, "해변 앞 카페가 많아서 쉬어 가기 편합니다.", 91),
            new Rating("B",  "busan", "광안리해수욕장", 3, "주말 저녁엔 사람이 많아 정신없었어요.", 139),

            // 속초: 날마다 두세 곳(설악산·수산시장·롯데리조트·만석닭강정은 운영에 이미 후기가 있다)
            new Rating(USER, "sokcho", "아바이마을", 4, "갯배 타는 게 짧지만 아이들이 제일 좋아했어요.", 0),
            new Rating("A",  "sokcho", "아바이마을", 3, "골목이 좁아서 주차가 조금 불편했습니다.", 120),
            new Rating(USER, "sokcho", "속초해수욕장", 5, "물이 맑고 모래가 고와서 오래 놀았어요.", 0),
            new Rating("B",  "sokcho", "속초해수욕장", 4, "시내에서 가까워 잠깐 들르기 좋아요.", 133),
            new Rating(USER, "sokcho", "청초호유원지", 5, "호수 둘레길이 평평해서 유모차 밀고 걷기 편했어요.", 0),
            new Rating("A",  "sokcho", "청초호유원지", 3, "날이 흐려서 설악산 쪽 전망은 잘 안 보였어요.", 125),
            new Rating(USER, "sokcho", "영금정", 5, "정자에 올라서 보는 바다가 시원했습니다.", 0),
            new Rating("A",  "sokcho", "영금정", 4, "파도가 센 날이라 바위에 부서지는 소리가 대단했어요.", 116),
            new Rating("B",  "sokcho", "영금정", 4, "속초등대까지 같이 묶어서 보면 좋아요.", 147),

            // 대구(다가오는 여행): 후기 전용 계정만
            new Rating("A",  "daegu", "서문시장", 4, "구경거리가 많아서 시간 가는 줄 몰랐어요.", 97),
            new Rating("B",  "daegu", "서문시장", 3, "주말엔 사람이 많아 천천히 다니기 어려웠습니다.", 128),
            new Rating("A",  "daegu", "청라언덕", 5, "선교사 주택이 있는 언덕 분위기가 조용하고 좋았어요.", 106),
            new Rating("B",  "daegu", "청라언덕", 4, "계산성당까지 걸어서 금방이에요.", 143),
            new Rating("A",  "daegu", "김광석다시그리기길", 5, "벽화 하나하나 보면서 천천히 걷기 좋았어요.", 99),
            new Rating("B",  "daegu", "김광석다시그리기길", 3, "길이 생각보다 짧아서 금방 다 봤어요.", 150),

            // 대전(다가오는 여행): 후기 전용 계정만
            new Rating("A",  "daejeon", "대전근현대사전시관", 4, "건물 자체가 볼거리라 사진 찍기 좋았습니다.", 108),
            new Rating("B",  "daejeon", "대전근현대사전시관", 3, "전시는 소박한 편이지만 잠깐 들러 볼 만해요.", 137),
            new Rating("A",  "daejeon", "성심당 본점", 5, "대전 오면 꼭 들르는 곳이에요.", 94),
            new Rating("B",  "daejeon", "성심당 본점", 4, "줄은 길었지만 생각보다 빨리 빠졌어요.", 124),
            new Rating("A",  "daejeon", "대전중앙시장", 3, "규모가 커서 길을 조금 헤맸어요.", 115),
            new Rating("B",  "daejeon", "대전중앙시장", 5, "시장 구경하면서 이것저것 맛보는 재미가 있었습니다.", 146)
    );
}
