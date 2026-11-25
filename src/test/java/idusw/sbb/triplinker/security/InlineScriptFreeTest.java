package idusw.sbb.triplinker.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CSP 에서 script-src 'unsafe-inline' 을 뺐으므로 화면 코드에 인라인 스크립트가 다시 들어오면 그 기능이 조용히 멈춘다.
 * 템플릿과 화면 JS 를 읽어 인라인 스크립트 · on* 속성 · 무결성 값 없는 외부 스크립트가 없는지 본다.
 */
class InlineScriptFreeTest {

    static final Path RES = Path.of("src/main/resources");

    // 주소에 판이 없어 내용이 바뀌는 로더라 무결성 값을 걸 수 없다(카카오 지도). 이 주소만 예외.
    static final List<String> SRI_EXEMPT = List.of("https://dapi.kakao.com/v2/maps/sdk.js");

    static final Pattern SCRIPT = Pattern.compile("<script\\b([^>]*)>", Pattern.CASE_INSENSITIVE);
    // 마크업 속성이든 JS 문자열 안의 HTML 이든: 공백 뒤 on이름= 다음 따옴표
    static final Pattern HANDLER_ATTR = Pattern.compile("(?<=\\s)on[a-z]+=(?=\\\\?[\"'])");

    private static List<Path> files(String dir, String ext) throws IOException {
        try (Stream<Path> s = Files.walk(RES.resolve(dir))) {
            return s.filter(p -> p.toString().endsWith(ext)).sorted().toList();
        }
    }

    // HTML 주석(<!-- -->) 안의 옛 코드는 실행되지 않으므로 템플릿에서는 지우고 본다
    private static String read(Path p) throws IOException {
        String s = Files.readString(p, StandardCharsets.UTF_8);
        return p.toString().endsWith(".html") ? s.replaceAll("(?s)<!--.*?-->", "") : s;
    }

    @Test
    @DisplayName("템플릿에 src 없는 <script> 가 없다")
    void noInlineScriptBlocks() throws IOException {
        List<String> found = new ArrayList<>();
        for (Path p : files("templates", ".html")) {
            Matcher m = SCRIPT.matcher(read(p));
            while (m.find()) {
                if (!m.group(1).contains("src=")) found.add(p.getFileName() + " " + m.group());
            }
        }
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("템플릿과 화면 JS 에 onclick 같은 인라인 이벤트 속성이 없다")
    void noInlineEventHandlerAttributes() throws IOException {
        List<Path> all = new ArrayList<>(files("templates", ".html"));
        all.addAll(files("static/js", ".js"));
        List<String> found = new ArrayList<>();
        for (Path p : all) {
            String s = read(p);
            Matcher m = HANDLER_ATTR.matcher(s);
            while (m.find()) {
                int line = (int) s.substring(0, m.start()).chars().filter(c -> c == '\n').count() + 1;
                found.add(p.getFileName() + ":" + line + " " + m.group());
            }
        }
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("외부 스크립트에는 무결성 값(SRI)이 붙어 있다 (판이 없는 카카오 지도 로더만 예외)")
    void externalScriptsHaveIntegrity() throws IOException {
        List<String> missing = new ArrayList<>();
        for (Path p : files("templates", ".html")) {
            Matcher m = SCRIPT.matcher(read(p));
            while (m.find()) {
                String attrs = m.group(1);
                Matcher src = Pattern.compile("\\ssrc=\"(https?://[^\"]+)\"").matcher(attrs);
                if (!src.find()) continue;
                String url = src.group(1);
                if (SRI_EXEMPT.stream().anyMatch(url::startsWith)) continue;
                if (!attrs.contains("integrity=\"sha")) missing.add(p.getFileName() + " " + url);
            }
        }
        assertThat(missing).isEmpty();
    }
}
