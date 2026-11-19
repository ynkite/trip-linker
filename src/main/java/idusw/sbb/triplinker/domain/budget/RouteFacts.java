package idusw.sbb.triplinker.domain.budget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 일정 JSON(routeJson)에서 예산에 필요한 사실만 뽑는다.
 *
 * 숙소 세기 — 일정 생성 규칙(「숙소 연속성」)상 둘째 날부터는 전날 숙소가 그날 맨 앞(출발 · 체크아웃)에
 * 한 번 더 나온다. 이 줄은 새 1박이 아니다. 그대로 세면 N박이 2N박이 된다.
 *
 * @param stayName  첫 숙소 이름(없으면 null)
 * @param stayNights 숙소 줄로 센 박 수(체크아웃 줄 제외)
 * @param tours     관광지 이름
 * @param transitKm 이동 거리 합(km, 카카오 길찾기가 채운 값)
 * @param transitFare 이동 금액 합(원, 일정에 적힌 값)
 * @param legs      이동 구간 수
 * @param walkLegs  그중 도보 구간 수(운임 0원)
 */
public record RouteFacts(String stayName, int stayNights, List<String> tours,
                         double transitKm, long transitFare, int legs, int walkLegs) {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final Pattern KM = Pattern.compile("([\\d.]+)\\s*km");
    private static final Pattern WON = Pattern.compile("₩([\\d,]+)");

    public static final RouteFacts EMPTY = new RouteFacts(null, 0, List.of(), 0, 0, 0, 0);

    /**
     * 체크아웃 줄인가 — 둘째 날부터, 그날 이동 줄을 뺀 첫 장소가 숙소면 전날 숙소에서 나서는 줄이다.
     * 일정 합계 · 가계부 예상 비용도 이 줄을 1박으로 세지 않는다.
     */
    public static boolean isCheckout(int day, JsonNode places, int index) {
        if (day <= 1 || places == null || !places.isArray()) return false;
        JsonNode node = places.get(index);
        if (node == null || !"stay".equals(node.path("type").asText(""))) return false;
        for (int i = 0; i < index; i++) {
            if (!places.get(i).has("transit")) return false;   // 앞에 다른 장소가 있으면 출발 줄이 아니다
        }
        return true;
    }

    public static RouteFacts parse(String routeJson) {
        if (routeJson == null || routeJson.isBlank()) return EMPTY;
        try {
            JsonNode root = OM.readTree(routeJson);
            if (!root.isArray()) return EMPTY;
            String stay = null;
            int nights = 0, legs = 0, walk = 0;
            double km = 0;
            long fare = 0;
            List<String> tours = new ArrayList<>();
            for (JsonNode day : root) {
                int d = day.path("day").asInt(1);
                JsonNode places = day.path("places");
                if (!places.isArray()) continue;
                for (int i = 0; i < places.size(); i++) {
                    JsonNode p = places.get(i);
                    if (p.has("transit")) {
                        String t = p.path("transit").asText("");
                        Matcher k = KM.matcher(t);
                        Matcher w = WON.matcher(t);
                        if (k.find()) km += Double.parseDouble(k.group(1));
                        if (w.find()) fare += Long.parseLong(w.group(1).replace(",", ""));
                        legs++;
                        if (t.contains("도보")) walk++;
                        continue;
                    }
                    String type = p.path("type").asText("");
                    String name = p.path("name").asText("").trim();
                    if ("stay".equals(type)) {
                        if (stay == null && !name.isEmpty()) stay = name;
                        if (!isCheckout(d, places, i)) nights++;
                    } else if ("tour".equals(type) && !name.isEmpty()) {
                        tours.add(name);
                    }
                }
            }
            return new RouteFacts(stay, nights, tours, Math.round(km * 10) / 10.0, fare, legs, walk);
        } catch (Exception e) {
            return EMPTY;
        }
    }
}
