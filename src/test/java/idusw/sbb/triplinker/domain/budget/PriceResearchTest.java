package idusw.sbb.triplinker.domain.budget;

import idusw.sbb.triplinker.domain.budget.BudgetCalculator.Admission;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 조사한 공개 요금 표 — 이름 · 지역 맞추기, 유형별 범위, 모르면 null. 실제 표가 아닌 시험용 줄로 본다. */
class PriceResearchTest {

    static final String HEAD = "sido,city,name,type,adult_fee,source_name,source_url,checked,quote";

    static PriceResearch table(String... rows) {
        return new PriceResearch(List.of(rows), HEAD);
    }

    @Test
    @DisplayName("표에 있는 장소는 그 금액 · 출처 기관 · 조회일이 나온다(조사한 공개 요금)")
    void foundInTable() {
        PriceResearch t = table("부산,해운대구,해운대블루라인파크|블루라인파크,유람선·열차,7000,부산 해운대구청,https://example.go.kr/a,2026-11-19,\"해변열차 1회 7,000원\"");
        Admission a = t.find("부산", "부산", "해운대블루라인파크 미포정거장");
        assertNotNull(a);
        assertEquals(7_000, a.min());
        assertEquals(7_000, a.max());
        assertEquals(Source.RESEARCHED, a.source());
        assertEquals("부산 해운대구청", a.sourceName());
        assertEquals("2026-11-19 조회", a.asOf());
        assertTrue(a.basis().contains("(표: 해운대블루라인파크)"), "이름이 다르게 맞았으면 표의 이름을 보인다: " + a.basis());
    }

    @Test
    @DisplayName("띄어쓰기 · 괄호가 달라도 같은 장소로 맞춘다")
    void spacingAndParens() {
        PriceResearch t = table("강원,속초,설악산국립공원,국립공원,0,국립공원공단,https://example.or.kr,2026-11-19,입장료 무료");
        assertNotNull(t.find("강원", "강원", "설악산 국립공원"));
        assertNotNull(t.find("강원", "강원", "설악산국립공원(소공원)"));
    }

    @Test
    @DisplayName("이름이 같아도 지역이 다르면 쓰지 않는다")
    void otherRegionNotUsed() {
        PriceResearch t = table("경남,통영,중앙시장,시장,0,통영시청,https://example.go.kr,2026-11-19,무료");
        assertNull(t.find("대전", "대전 중구", "중앙시장"));
        assertNull(t.find(null, "서울", "중앙시장"), "시도를 모르면 여행지 글의 시군 이름이 맞아야 한다");
        assertNotNull(t.find(null, "통영", "중앙시장"));
    }

    @Test
    @DisplayName("짧은 일반 이름(공원 · 시장)이 긴 이름 속에 들었다고 맞추지 않는다")
    void shortGenericNameDoesNotMatch() {
        PriceResearch t = table("대구,중구,공원,공원,0,대구시청,https://example.go.kr,2026-11-19,무료");
        assertNull(t.find("대구", "대구 중구", "달성공원"));
    }

    @Test
    @DisplayName("표에 없고 유형을 알면 같은 유형 · 같은 시도 조사값의 최저~최고(조사 추정)")
    void typeRangeSameRegion() {
        PriceResearch t = table(
                "부산,해운대구,부산아쿠아리움,수족관,32000,A,https://a,2026-11-19,성인 32000원",
                "서울,강남구,코엑스아쿠아리움,수족관,35000,B,https://b,2026-11-19,성인 35000원",
                "부산,해운대구,해운대해수욕장,해수욕장,0,C,https://c,2026-11-19,무료",
                "부산,수영구,광안리해수욕장,해수욕장,0,D,https://d,2026-11-19,무료");
        Admission beach = t.estimate("부산", "부산", "송정해수욕장");
        assertNotNull(beach);
        assertEquals(Source.RESEARCH_ESTIMATE, beach.source());
        assertEquals(0, beach.min());
        assertEquals(0, beach.max());
        assertTrue(beach.basis().contains("해수욕장"), beach.basis());
        assertTrue(beach.basis().contains("2곳"), beach.basis());
    }

    @Test
    @DisplayName("같은 시도에 그 유형이 없으면 전국 같은 유형 조사값의 범위, 근거에 「전국」 을 적는다")
    void typeRangeNationwide() {
        PriceResearch t = table(
                "부산,해운대구,부산아쿠아리움,수족관,32000,A,https://a,2026-11-19,성인 32000원",
                "서울,강남구,코엑스아쿠아리움,수족관,35000,B,https://b,2026-11-19,성인 35000원");
        Admission a = t.estimate("제주", "제주 서귀포시", "아쿠아플라넷 제주");
        assertNotNull(a);
        assertEquals(32_000, a.min());
        assertEquals(35_000, a.max());
        assertTrue(a.basis().contains("전국"), a.basis());
    }

    @Test
    @DisplayName("유형을 모르거나 그 유형 조사값이 없으면 null(미산정)")
    void unknownTypeIsNull() {
        PriceResearch t = table("부산,해운대구,해운대해수욕장,해수욕장,0,C,https://c,2026-11-19,무료");
        assertNull(t.estimate("부산", "부산", "동백섬"), "유형을 알 수 없는 이름");
        assertNull(t.estimate("부산", "부산", "부산시립미술관"), "미술관 조사값이 없음");
        assertNull(PriceResearch.type("계산성당"));
        assertEquals("박물관", PriceResearch.type("대전근현대사전시관"));
        assertEquals("거리", PriceResearch.type("김광석다시그리기길"));
        assertEquals("공원", PriceResearch.type("청초호유원지"));
        assertEquals("사찰", PriceResearch.type("불국사"));
    }

    @Test
    @DisplayName("금액 칸이 빈 줄(못 찾은 장소)은 쓰지 않는다")
    void blankFeeRowIgnored() {
        PriceResearch t = table("부산,해운대구,동백섬,기타,,-,-,2026-11-19,못 찾음");
        assertNull(t.find("부산", "부산", "동백섬"));
    }

    @Test
    @DisplayName("저장소의 실제 표가 읽히고 모든 줄에 출처 주소 · 조회일 · 확인한 문장이 있다")
    void realTableHasSources() {
        PriceResearch t = PriceResearch.load();
        assertTrue(t.size() > 0);
        assertTrue(t.rows().stream().allMatch(r -> r.url().startsWith("http") && !r.checked().isBlank() && !r.quote().isBlank()));
    }
}
