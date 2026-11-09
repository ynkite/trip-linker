package idusw.sbb.triplinker.domain.budget.dto;

import java.util.List;

/**
 * 공공 자료로 낸 예상 예산. 숫자 하나가 아니라 항목마다 범위 · 근거 · 출처 · 기준일 · 가격 상태를 같이 준다.
 * 미산정 항목은 합계에 넣지 않는다 — 합계는 「계산한 항목만의 합」 이다.
 *
 * @param items          항목(숙박 · 식비 · 교통 · 입장료)
 * @param min            계산한 항목 합계 최소
 * @param max            계산한 항목 합계 최대
 * @param publicShare    합계(가운데 값) 중 관광공사 실시간 자료(공개 요금 · 통계 기반 추정) 비율(%) — 내림
 * @param confidence     높음 · 보통 · 낮음
 * @param missing        미산정 항목 수 — 0 이 아니면 합계는 부분 합계
 * @param userBudget     사용자가 정한 예산(없으면 null)
 * @param budgetCheck    사용자 예산과 범위 비교 한 줄
 * @param excluded       계산 대상이 아닌 비용 — 화면에 그대로 적는다
 * @param notes          키 없음 · 고지 같은 안내
 * @param tourApi        관광공사 키가 켜져 있었는지
 */
public record BudgetEstimate(
        List<Item> items,
        long min,
        long max,
        int publicShare,
        String confidence,
        int missing,
        Long userBudget,
        String budgetCheck,
        List<String> excluded,
        List<String> notes,
        boolean tourApi
) {
    /** 가격 상태. 공개 요금도 예약 확정가가 아니다(관광공사에 공개된 객실 최저요금 · 이용요금). */
    public enum Source {
        PUBLIC_RATE("확인된 공개 요금"),       // 관광공사 숙소 객실 요금 · 관광지 이용요금
        STAT_ESTIMATE("통계 기반 추정"),       // 관광공사 같은 지역 숙소 표본
        ASSUMED("가정"),                       // 원문을 확인 못 한 단가 · 우리가 정한 범위
        NOT_ESTIMATED("미산정");               // 근거가 없어 계산하지 않음 — 합계 제외

        public final String label;

        Source(String label) { this.label = label; }

        /** 관광공사에서 실시간으로 받은 자료인가. */
        public boolean live() { return this == PUBLIC_RATE || this == STAT_ESTIMATE; }
    }

    /**
     * @param category   STAY · FOOD · TRANSPORT · TOUR (가계부 분류와 같다)
     * @param label      「2박 숙박 · 방 1개」 같은 이름
     * @param min        최소(원). 미산정이면 0
     * @param max        최대(원). 미산정이면 0
     * @param basis      계산식 그대로 — 「1실 1박 45,000~80,000원 × 방 1개 × 2박」
     * @param source     가격 상태
     * @param sourceName 기관 · 자료 이름
     * @param asOf       기준일 또는 조회일
     */
    public record Item(String category, String label, long min, long max,
                       String basis, Source source, String sourceName, String asOf) {
        @com.fasterxml.jackson.annotation.JsonProperty("sourceLabel")   // 레코드 구성요소가 아니라서 붙여야 JSON 에 나간다
        public String sourceLabel() { return source.label; }
    }
}
