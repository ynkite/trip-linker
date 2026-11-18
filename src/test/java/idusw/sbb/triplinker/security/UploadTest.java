package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.domain.post.controller.PostController;
import idusw.sbb.triplinker.domain.post.service.PostService;
import idusw.sbb.triplinker.domain.system.service.SystemService;
import idusw.sbb.triplinker.global.exception.GlobalExceptionHandler;
import idusw.sbb.triplinker.global.service.LocalFileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * OWASP A03/A04 — 커뮤니티 이미지 올리기. 올린 파일은 /uploads/ 로 같은 도메인에서 그대로 내려간다.
 * HTML · SVG 를 올릴 수 있으면 그 주소를 연 사람의 브라우저에서 스크립트가 돈다(저장형 XSS).
 */
class UploadTest {

    // 1x1 투명 PNG
    static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=");

    @TempDir
    Path dir;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        LocalFileService files = new LocalFileService();
        ReflectionTestUtils.setField(files, "uploadPath", dir.toString() + File.separator);
        ReflectionTestUtils.setField(files, "uploadUrl", "/uploads/posts/");
        mvc = MockMvcBuilders.standaloneSetup(new PostController(mock(PostService.class), mock(SystemService.class), files))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        TripFixture.loginAs(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private long savedCount() throws Exception {
        try (var s = Files.list(dir)) { return s.count(); }
    }

    @Test
    @DisplayName("HTML 파일은 올라가지 않는다")
    void htmlRejected() throws Exception {
        mvc.perform(multipart("/api/posts/images").file(new MockMultipartFile("files", "a.html", "text/html",
                "<script>alert(document.domain)</script>".getBytes()))).andExpect(status().isBadRequest());
        assertThat(savedCount()).isZero();
    }

    @Test
    @DisplayName("SVG 파일은 올라가지 않는다 (SVG 안에 스크립트를 넣을 수 있음)")
    void svgRejected() throws Exception {
        mvc.perform(multipart("/api/posts/images").file(new MockMultipartFile("files", "a.svg", "image/svg+xml",
                "<svg xmlns='http://www.w3.org/2000/svg' onload='alert(1)'/>".getBytes()))).andExpect(status().isBadRequest());
        assertThat(savedCount()).isZero();
    }

    @Test
    @DisplayName("이름만 .png 인 HTML 도 막는다 (파일 앞머리로 확인)")
    void disguisedHtmlRejected() throws Exception {
        mvc.perform(multipart("/api/posts/images").file(new MockMultipartFile("files", "a.png", "image/png",
                "<html><script>alert(1)</script>".getBytes()))).andExpect(status().isBadRequest());
        assertThat(savedCount()).isZero();
    }

    @Test
    @DisplayName("정상: PNG 사진은 올라간다")
    void pngAccepted() throws Exception {
        mvc.perform(multipart("/api/posts/images").file(new MockMultipartFile("files", "photo.PNG", "image/png", PNG)))
                .andExpect(status().isOk());
        assertThat(savedCount()).isEqualTo(1);
    }
}
