package idusw.sbb.triplinker.security;

import idusw.sbb.triplinker.global.controller.PageController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 첫 화면(/)을 Thymeleaf 로 실제로 그려, 브라우저가 받는 HTML 에 인라인 스크립트 · on* 속성이 없는지 본다.
 * (템플릿 파일 검사 InlineScriptFreeTest 와 달리 th:insert 로 합쳐진 결과를 본다)
 */
class IndexPageRenderTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        ThymeleafViewResolver views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");

        PageController page = new PageController();
        ReflectionTestUtils.setField(page, "kakaoMapKey", "test");
        mvc = MockMvcBuilders.standaloneSetup(page).setViewResolvers(views).build();
    }

    private String render() throws Exception {
        return mvc.perform(get("/")).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("그린 첫 화면의 <script> 는 모두 src 가 있는 파일이다")
    void everyScriptHasSrc() throws Exception {
        String html = render().replaceAll("(?s)<!--.*?-->", "");
        Matcher m = Pattern.compile("<script\\b([^>]*)>").matcher(html);
        int n = 0;
        while (m.find()) {
            n++;
            assertThat(m.group(1)).as(m.group()).contains("src=\"");
        }
        assertThat(n).isGreaterThan(10);
        assertThat(html).contains("src=\"/js/page/page_map_1.js\"");
    }

    @Test
    @DisplayName("그린 첫 화면에 onclick 같은 인라인 이벤트 속성이 없고, data-on* 를 실행하는 스크립트가 맨 먼저 온다")
    void noInlineHandlersAndRuntimeFirst() throws Exception {
        String html = render().replaceAll("(?s)<!--.*?-->", "");
        // 태그 안의 속성만 본다(스크립트 파일 내용은 HTML 에 없음)
        Matcher tag = Pattern.compile("<[a-zA-Z][^>]*>").matcher(html);
        while (tag.find()) {
            assertThat(tag.group()).as(tag.group()).doesNotContainPattern("\\son[a-z]+\\s*=");
        }
        assertThat(html).contains("data-onclick=");
        int runtime = html.indexOf("/js/inline_handlers.js");
        int firstOther = html.indexOf("https://dapi.kakao.com");
        assertThat(runtime).isPositive().isLessThan(firstOther);
        assertThat(html.indexOf("/js/inline_handlers_table.js")).isPositive().isLessThan(runtime);
    }
}
