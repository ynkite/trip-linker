package idusw.sbb.triplinker.domain.demo.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 테스트 관리자가 관리자 API(/api/admin/**)를 읽을 때 다른 회원의 이름·이메일·아이디·전화번호를 가려서 내려준다.
 * 진짜 관리자와 일반 회원 응답은 건드리지 않는다.
 *
 * 화면이 쓰는 키 이름은 그대로 두고 값만 바꾼다(예: 김민준 → 김**, minjun@test.com → m***@test.com).
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class DemoAdminMaskingAdvice implements ResponseBodyAdvice<Object> {

    /** 값이 무엇이든 가리는 키 */
    private static final Set<String> ALWAYS = Set.of(
            "email", "username", "phone", "phoneNumber", "tel", "nickname",
            "reporterName", "commentAuthorName", "postAuthorName", "processedByName",
            "writerName", "authorName", "userName", "realName");

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null || !isDemoAdmin()) return body;
        String path = request.getURI().getPath();
        if (path == null || !path.contains("/api/admin")) return body;

        JsonNode tree = objectMapper.valueToTree(body);
        mask(tree);
        return tree;
    }

    private static boolean isDemoAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + DemoLoginProperties.ROLE_DEMO_ADMIN).equals(a.getAuthority()));
    }

    static void mask(JsonNode node) {
        if (node == null) return;
        if (node.isArray()) {
            for (JsonNode child : node) mask(child);
            return;
        }
        if (!node.isObject()) return;

        ObjectNode obj = (ObjectNode) node;
        // 회원 한 명을 나타내는 객체(이메일이나 아이디가 같이 있는 것)의 name 도 가린다.
        boolean personLike = obj.has("email") || obj.has("username");
        List<Map.Entry<String, JsonNode>> fields = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
        while (it.hasNext()) fields.add(it.next());

        for (Map.Entry<String, JsonNode> f : fields) {
            String key = f.getKey();
            JsonNode value = f.getValue();
            if (value.isTextual() && (ALWAYS.contains(key) || (personLike && "name".equals(key)))) {
                obj.put(key, maskValue(key, value.asText()));
            } else if (value.isContainerNode()) {
                mask(value);
            }
        }
    }

    static String maskValue(String key, String v) {
        if (v == null || v.isEmpty()) return v;
        if (key.toLowerCase().contains("email") || v.contains("@")) {
            int at = v.indexOf('@');
            if (at <= 0) return "***";
            return v.charAt(0) + "***" + v.substring(at);
        }
        if (key.toLowerCase().contains("phone") || "tel".equals(key)) {
            return "***-****-****";
        }
        return v.charAt(0) + "*".repeat(Math.max(2, v.length() - 1));
    }
}
