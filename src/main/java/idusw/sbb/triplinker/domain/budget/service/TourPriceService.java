package idusw.sbb.triplinker.domain.budget.service;

import com.fasterxml.jackson.databind.JsonNode;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.Admission;
import idusw.sbb.triplinker.domain.budget.BudgetCalculator.LodgingQuote;
import idusw.sbb.triplinker.domain.budget.client.TourApiClient;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 관광공사 TourAPI 로 숙박 요금 · 관광지 이용요금을 찾는다. 못 찾으면 null — 계산기가 「가정」 으로 내려간다.
 *
 * 숙박(checkin 저장소 LodgingRateService 를 옮겨 고침):
 *   1) 일정의 숙소를 이름으로 찾아(searchKeyword2, 숙박 32) 객실별 요금(detailInfo2)의
 *      여행 날짜 칸(성수기/비수기 × 주중/주말) 최저요금들 → 가장 싼 방 ~ 가장 비싼 방.
 *   2) 요금 칸이 비었으면 같은 시도 숙소 표본 12곳(areaBasedList2)의 같은 칸 →
 *      4곳 이상이면 가운데 50%(25~75번째 백분위), 적으면 최저~최고. 고정 ±% 를 쓰지 않는다.
 */
@Service
@RequiredArgsConstructor
public class TourPriceService {

    static final int SAMPLE = 12;
    private static final Pattern WON = Pattern.compile("(\\d{1,3}(?:,\\d{3})+|\\d{3,})\\s*원");

    private final TourApiClient client;

    public boolean enabled() {
        return client.enabled();
    }

    // ── 숙박 ──

    public LodgingQuote lodging(String stayName, String areaCode, LocalDate checkIn) {
        if (!client.enabled()) return null;
        boolean peak = isPeak(checkIn), weekend = isWeekend(checkIn);
        String field = feeField(peak, weekend);
        String label = (peak ? "성수기 " : "비수기 ") + (weekend ? "주말" : "주중");
        String today = LocalDate.now().toString();

        String contentId = find(stayName, "32", areaCode);
        if (contentId != null) {
            List<Long> fees = roomFees(contentId, field);
            if (!fees.isEmpty()) {
                return new LodgingQuote(fees.get(0), fees.get(fees.size() - 1), Source.PUBLIC_RATE,
                        "「" + stayName + "」 객실 " + fees.size() + "개 " + label + " 최저요금",
                        "한국관광공사 TourAPI 숙박 요금", today + " 조회");
            }
        }

        Map<String, String> p = new LinkedHashMap<>();
        p.put("numOfRows", String.valueOf(SAMPLE));
        p.put("pageNo", "1");
        p.put("arrange", "A");
        p.put("contentTypeId", "32");
        if (areaCode != null) p.put("areaCode", areaCode);
        List<Long> sample = new ArrayList<>();
        for (JsonNode s : TourApiClient.list(client.items("areaBasedList2", p))) {
            String cid = s.path("contentid").asText("");
            if (cid.isBlank()) continue;
            List<Long> fees = roomFees(cid, field);
            if (!fees.isEmpty()) sample.add(fees.get(0));   // 숙소마다 가장 싼 방
        }
        if (sample.isEmpty()) return null;
        sample.sort(Long::compare);
        int n = sample.size();
        boolean iqr = n >= 4;
        long lo = iqr ? percentile(sample, 25) : sample.get(0);
        long hi = iqr ? percentile(sample, 75) : sample.get(n - 1);
        return new LodgingQuote(lo, hi, Source.STAT_ESTIMATE,
                "같은 지역 숙소 " + n + "곳 " + label + " 최저요금 " + (iqr ? "가운데 50%" : "최저~최고"),
                "한국관광공사 TourAPI 숙박 요금", today + " 조회");
    }

    /** 객실들의 그 칸 요금(0 초과) 오름차순. */
    List<Long> roomFees(String contentId, String field) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("contentId", contentId);
        p.put("contentTypeId", "32");
        p.put("numOfRows", "30");
        List<Long> out = new ArrayList<>();
        for (JsonNode r : TourApiClient.list(client.items("detailInfo2", p))) {
            long v = parseLong(r.path(field).asText(""));
            if (v > 0) out.add(v);
        }
        out.sort(Long::compare);
        return out;
    }

    // ── 관광지 이용요금 ──

    /**
     * 관광지 이름 → 1인 이용요금. 문화시설 usefee · 레포츠 usefeeleports(detailIntro2),
     * 그 밖엔 detailInfo2 의 「입장료 · 이용요금」 줄. 「무료」 면 0, 금액이 있으면 가장 싼 값 ~ 가장 비싼 값.
     * 못 찾거나 글에 금액이 없으면 null.
     */
    public Admission admission(String name, String areaCode) {
        if (!client.enabled()) return null;
        Map<String, String> q = new LinkedHashMap<>();
        q.put("keyword", name);
        q.put("numOfRows", "1");
        q.put("pageNo", "1");
        q.put("arrange", "A");
        if (areaCode != null) q.put("areaCode", areaCode);
        List<JsonNode> hit = TourApiClient.list(client.items("searchKeyword2", q));
        if (hit.isEmpty()) return null;
        String cid = hit.get(0).path("contentid").asText("");
        String type = hit.get(0).path("contenttypeid").asText("");
        if (cid.isBlank()) return null;

        Map<String, String> p = new LinkedHashMap<>();
        p.put("contentId", cid);
        p.put("contentTypeId", type);
        StringBuilder text = new StringBuilder();
        for (JsonNode n : TourApiClient.list(client.items("detailIntro2", p))) {
            text.append(n.path("usefee").asText("")).append(' ').append(n.path("usefeeleports").asText(""));
        }
        if (text.toString().isBlank()) {
            for (JsonNode n : TourApiClient.list(client.items("detailInfo2", p))) {
                String k = n.path("infoname").asText("");
                if (k.contains("요금") || k.contains("입장료") || k.contains("관람료")) text.append(n.path("infotext").asText("")).append(' ');
            }
        }
        return parseFee(name, text.toString());
    }

    /** 이용요금 글 → Admission. 금액 · 무료 표시가 없으면 null. */
    static Admission parseFee(String name, String raw) {
        String text = raw == null ? "" : raw.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        if (text.isEmpty()) return null;
        List<Long> won = new ArrayList<>();
        Matcher m = WON.matcher(text);
        while (m.find()) won.add(Long.parseLong(m.group(1).replace(",", "")));
        String shown = text.length() > 40 ? text.substring(0, 40) + "…" : text;
        if (won.isEmpty()) {
            return text.contains("무료")
                    ? new Admission(name, 0, 0, Source.PUBLIC_RATE, "관광공사 이용요금 「" + shown + "」")
                    : null;
        }
        won.sort(Long::compare);
        long lo = text.contains("무료") ? 0 : won.get(0);
        return new Admission(name, lo, won.get(won.size() - 1), Source.PUBLIC_RATE, "관광공사 이용요금 「" + shown + "」");
    }

    // ── 공통 ──

    private String find(String name, String contentTypeId, String areaCode) {
        if (name == null || name.isBlank()) return null;
        Map<String, String> q = new LinkedHashMap<>();
        q.put("keyword", name);
        q.put("numOfRows", "1");
        q.put("pageNo", "1");
        q.put("arrange", "A");
        q.put("contentTypeId", contentTypeId);
        if (areaCode != null) q.put("areaCode", areaCode);
        List<JsonNode> hit = TourApiClient.list(client.items("searchKeyword2", q));
        String cid = hit.isEmpty() ? "" : hit.get(0).path("contentid").asText("");
        return cid.isBlank() ? null : cid;
    }

    static boolean isPeak(LocalDate d) {
        int m = d.getMonthValue();
        return m == 7 || m == 8;   // 관광공사 요금표의 「성수기」 칸을 쓰는 기준(여름)
    }

    static boolean isWeekend(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        return w == DayOfWeek.FRIDAY || w == DayOfWeek.SATURDAY;   // 체크인 기준
    }

    static String feeField(boolean peak, boolean weekend) {
        if (peak) return weekend ? "roompeakseasonminfee2" : "roompeakseasonminfee1";
        return weekend ? "roomoffseasonminfee2" : "roomoffseasonminfee1";
    }

    /** 가장 가까운 순위 백분위(nearest-rank). */
    static long percentile(List<Long> sorted, int pct) {
        int rank = (int) Math.ceil(pct / 100.0 * sorted.size());
        return sorted.get(Math.max(0, Math.min(sorted.size() - 1, rank - 1)));
    }

    private static long parseLong(String s) {
        try { return Long.parseLong(s.replace(",", "").trim()); }
        catch (Exception e) { return 0; }
    }
}
