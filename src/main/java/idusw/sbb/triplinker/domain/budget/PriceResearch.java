package idusw.sbb.triplinker.domain.budget;

import idusw.sbb.triplinker.domain.budget.BudgetCalculator.Admission;
import idusw.sbb.triplinker.domain.budget.dto.BudgetEstimate.Source;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 관광공사에 이용요금이 없을 때 쓰는 조사한 공개 요금 표(resources/budget/price_research.csv).
 *
 * 표의 금액은 공식 누리집 · 지자체 · 공공기관 안내에서 직접 읽은 성인 1인 요금이고, 줄마다 출처 주소 · 조회일 · 그 문장을 같이 둔다.
 * 실행 중에 웹을 찾거나 AI 에게 묻지 않는다 — 값마다 출처를 보일 수 있어야 해서다.
 *
 *   find      이름 · 지역이 맞는 줄의 금액(조사한 공개 요금). 띄어쓰기 · 괄호는 무시하고, 한쪽 이름이 다른 쪽을 품으면
 *             (짧은 쪽 4자 이상) 같은 곳으로 본다. 지역이 다르면 이름이 같아도 쓰지 않는다.
 *   estimate  표에 없으면 이름으로 유형(박물관 · 해수욕장 …)을 가려 같은 유형 조사값의 최저~최고(조사 추정).
 *             같은 시도 값이 있으면 그것만, 없으면 전국 값(2곳 이상일 때). 유형을 모르면 null → 미산정.
 */
@Component
public class PriceResearch {

    static final String RESOURCE = "budget/price_research.csv";
    private static final int MIN_CONTAINED = 4;

    /** @param names 이름과 다른 이름들(표의 name 칸을 | 로 나눔) */
    public record Row(String sido, String city, List<String> names, String type, long fee,
                      String sourceName, String url, String checked, String quote) {}

    /** 이름 속 낱말 → 유형. 앞에 있는 것이 먼저(「전시관」 이 「…사」 보다 먼저). */
    private static final List<Map.Entry<String, String>> TYPE_WORDS = List.of(
            Map.entry("케이블카", "케이블카"), Map.entry("아쿠아", "수족관"), Map.entry("수족관", "수족관"),
            Map.entry("전망대", "전망대"), Map.entry("타워", "전망대"),
            Map.entry("박물관", "박물관"), Map.entry("전시관", "박물관"), Map.entry("기념관", "박물관"),
            Map.entry("미술관", "미술관"), Map.entry("국립공원", "국립공원"),
            Map.entry("해수욕장", "해수욕장"), Map.entry("시장", "시장"),
            Map.entry("테마파크", "테마파크"), Map.entry("놀이공원", "테마파크"),   // 「…랜드 · …월드」 는 섬 · 경기장과 헷갈려 쓰지 않는다
            Map.entry("공원", "공원"), Map.entry("유원지", "공원"),
            Map.entry("거리", "거리"), Map.entry("골목", "거리"));

    private final List<Row> rows;

    /** 저장소의 표를 읽는다. 파일이 없으면 빈 표. */
    public PriceResearch() {
        this(readResource(), null);
    }

    /**
     * @param lines  자료 줄(머리 줄 없이, header 가 null 이면 lines 의 첫 줄이 머리 줄)
     * @param header 열 이름 줄
     */
    public PriceResearch(List<String> lines, String header) {
        List<String> body = new ArrayList<>(lines);
        String head = header != null ? header : body.isEmpty() ? "" : body.remove(0);
        List<String> cols = csv(head);
        List<Row> out = new ArrayList<>();
        for (String line : body) {
            if (line.isBlank() || line.startsWith("#")) continue;
            List<String> v = csv(line);
            String fee = col(cols, v, "adult_fee");
            if (fee.isBlank()) continue;                       // 못 찾은 장소는 표에 남겨도 쓰지 않는다
            out.add(new Row(col(cols, v, "sido"), col(cols, v, "city"),
                    Arrays.stream(col(cols, v, "name").split("\\|")).map(String::trim).filter(s -> !s.isEmpty()).toList(),
                    col(cols, v, "type"), Long.parseLong(fee.replace(",", "").trim()),
                    col(cols, v, "source_name"), col(cols, v, "source_url"), col(cols, v, "checked"), col(cols, v, "quote")));
        }
        this.rows = List.copyOf(out);
    }

    public static PriceResearch load() {
        return new PriceResearch();
    }

    public int size() { return rows.size(); }

    public List<Row> rows() { return rows; }

    /** 조사한 공개 요금. 이름 · 지역이 맞는 줄이 없으면 null. */
    public Admission find(String sido, String destination, String name) {
        String key = norm(name);
        if (key.isEmpty()) return null;
        Row best = null;
        String bestName = null;
        int bestLen = -1;
        for (Row r : rows) {
            if (!sameRegion(r, sido, destination)) continue;
            for (String n : r.names()) {
                String k = norm(n);
                if (matches(key, k) && k.length() > bestLen) { best = r; bestName = n; bestLen = k.length(); }
            }
        }
        if (best == null) return null;
        return new Admission(name, best.fee(), best.fee(), Source.RESEARCHED,
                name + (norm(bestName).equals(key) ? "" : "(표: " + bestName + ")") + " " + fee(best.fee()),
                best.sourceName(), best.checked() + " 조회");
    }

    /** 같은 유형 조사값의 범위. 유형을 모르거나 그 유형 값이 없으면 null. */
    public Admission estimate(String sido, String destination, String name) {
        String type = type(name);
        if (type == null) return null;
        List<Row> same = rows.stream().filter(r -> r.type().equals(type)).toList();
        List<Row> near = same.stream().filter(r -> sameRegion(r, sido, destination)).toList();
        boolean local = !near.isEmpty();
        List<Row> use = local ? near : same;
        if (use.isEmpty() || (!local && use.size() < 2)) return null;
        long lo = use.stream().mapToLong(Row::fee).min().orElse(0);
        long hi = use.stream().mapToLong(Row::fee).max().orElse(0);
        String checked = use.stream().map(Row::checked).max(Comparator.naturalOrder()).orElse("");
        return new Admission(name, lo, hi, Source.RESEARCH_ESTIMATE,
                name + "(" + type + ") " + (lo == hi ? fee(lo) : fee(lo) + "~" + fee(hi))
                        + " — " + (local ? "같은 지역" : "전국") + " " + type + " 조사값 " + use.size() + "곳",
                "조사한 공개 요금(같은 유형 범위)", checked + " 조회");
    }

    /** 이름으로 가린 유형. 모르면 null. */
    public static String type(String name) {
        String n = norm(name);
        if (n.isEmpty()) return null;
        for (Map.Entry<String, String> e : TYPE_WORDS) if (n.contains(e.getKey())) return e.getValue();
        if (n.endsWith("길")) return "거리";
        if (n.endsWith("사") || n.endsWith("사찰")) return "사찰";
        return null;
    }

    // ── 안쪽 ──

    private static boolean matches(String a, String b) {
        if (a.equals(b)) return true;
        String s = a.length() <= b.length() ? a : b, l = s == a ? b : a;
        return s.length() >= MIN_CONTAINED && l.contains(s);
    }

    /** 시도를 알면 시도가 같아야 하고, 모르면 여행지 글의 첫 낱말(시 · 군 뗌)이 표의 시군과 같아야 한다. */
    private static boolean sameRegion(Row r, String sido, String destination) {
        if (sido != null) return sido.equals(r.sido());
        if (destination == null || destination.isBlank()) return false;
        String first = destination.trim().split("\\s+")[0];
        return !r.city().isBlank() && city(first).equals(city(r.city()));
    }

    private static String city(String s) {
        return s.length() > 2 && (s.endsWith("시") || s.endsWith("군")) ? s.substring(0, s.length() - 1) : s;
    }

    static String norm(String s) {
        return s == null ? "" : s.replaceAll("\\([^)]*\\)", "").replaceAll("[\\s·]", "").toLowerCase();
    }

    private static String fee(long v) {
        return v == 0 ? "무료" : String.format("%,d원", v);
    }

    private static String col(List<String> cols, List<String> v, String name) {
        int i = cols.indexOf(name);
        return i >= 0 && i < v.size() ? v.get(i).trim() : "";
    }

    /** 쉼표 칸 나누기 — 큰따옴표로 감싼 칸 안의 쉼표 · 겹 따옴표("")를 처리한다. */
    static List<String> csv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (q) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else if (c == '"') q = false;
                else cur.append(c);
            } else if (c == '"') q = true;
            else if (c == ',') { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        out.add(cur.toString());
        return out;
    }

    private static List<String> readResource() {
        try (InputStream in = PriceResearch.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) return List.of();
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (text.startsWith("﻿")) text = text.substring(1);
            return List.of(text.split("\\r?\\n"));
        } catch (Exception e) {
            return List.of();
        }
    }
}
