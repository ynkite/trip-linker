package idusw.sbb.triplinker.domain.demo.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import idusw.sbb.triplinker.domain.admin.dto.AdminUserListResponseDto;
import idusw.sbb.triplinker.domain.auth.security.CustomUserDetails;
import idusw.sbb.triplinker.global.common.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DemoAdminMaskingAdviceTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final DemoAdminMaskingAdvice advice = new DemoAdminMaskingAdvice(mapper);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String role) {
        CustomUserDetails p = new CustomUserDetails(5L, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));
    }

    private Object write(Object body, String path) {
        return advice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(new MockHttpServletRequest("GET", path)),
                new ServletServerHttpResponse(new MockHttpServletResponse()));
    }

    private Object usersPage() {
        return ApiResponse.success(Map.of("content", List.of(
                new AdminUserListResponseDto(3L, "user01", "김민준", "minjun@test.com",
                        "ACTIVE", "USER", LocalDateTime.of(2026, 1, 1, 0, 0)))));
    }

    @Test
    @DisplayName("테스트 관리자에게는 회원 이름·이메일·아이디가 가려져 내려간다")
    void masksForDemoAdmin() {
        loginAs("DEMO_ADMIN");
        JsonNode out = (JsonNode) write(usersPage(), "/api/admin/users");
        JsonNode row = out.get("data").get("content").get(0);

        assertThat(row.get("name").asText()).isEqualTo("김**");
        assertThat(row.get("email").asText()).isEqualTo("m***@test.com");
        assertThat(row.get("username").asText()).isEqualTo("u*****");
        assertThat(out.toString()).doesNotContain("김민준", "minjun@", "user01");
        // 화면이 쓰는 다른 값은 그대로
        assertThat(row.get("userId").asLong()).isEqualTo(3L);
        assertThat(row.get("status").asText()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("신고 목록의 신고자·작성자·처리자 이름도 가린다. 게시글 제목은 그대로")
    void masksReportNames() {
        loginAs("DEMO_ADMIN");
        Map<String, Object> report = Map.of("postTitle", "제주 후기", "reporterName", "이서연",
                "postAuthorName", "박지호", "processedByName", "관리자", "phone", "010-1234-5678");
        JsonNode out = (JsonNode) write(ApiResponse.success(List.of(report)), "/api/admin/reports");
        JsonNode row = out.get("data").get(0);
        assertThat(row.get("reporterName").asText()).isEqualTo("이**");
        assertThat(row.get("postAuthorName").asText()).isEqualTo("박**");
        assertThat(row.get("phone").asText()).isEqualTo("***-****-****");
        assertThat(row.get("postTitle").asText()).isEqualTo("제주 후기");
    }

    @Test
    @DisplayName("진짜 관리자, 또는 관리자 API 가 아닌 응답은 손대지 않는다")
    void untouchedOtherwise() {
        Object body = usersPage();
        loginAs("ADMIN");
        assertThat(write(body, "/api/admin/users")).isSameAs(body);

        loginAs("DEMO_ADMIN");
        assertThat(write(body, "/api/users/me")).isSameAs(body);
    }
}
