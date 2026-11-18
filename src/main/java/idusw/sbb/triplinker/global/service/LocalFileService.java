package idusw.sbb.triplinker.global.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class LocalFileService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url}")
    private String uploadUrl;

    // 올린 파일은 /uploads/ 로 같은 도메인에서 그대로 내려가므로 사진 형식만 받는다.
    // (전에는 확장자를 보지 않아 .html · .svg 를 올리면 그 주소를 연 사람의 브라우저에서 스크립트가 돌 수 있었다)
    private static final Map<String, String> ALLOWED = Map.of(
            "jpg", "jpg", "jpeg", "jpg", "png", "png", "gif", "gif", "webp", "webp");

    public String save(MultipartFile file) throws IOException {
        String ext = extensionOf(file.getOriginalFilename());
        String kind = ALLOWED.get(ext);
        if (kind == null || !kind.equals(sniff(file))) {
            throw new IllegalArgumentException("사진 파일(jpg, png, gif, webp)만 올릴 수 있습니다.");
        }

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 저장 이름은 서버가 정한다(원래 이름은 확장자만 확인용으로 씀)
        String savedFilename = UUID.randomUUID() + "." + ext;
        file.transferTo(new File(uploadPath + savedFilename));

        return uploadUrl + savedFilename;
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(uploadUrl)) {
            return;
        }
        String filename = fileUrl.substring(uploadUrl.length());
        // 업로드 폴더 밖(../)을 가리키면 지우지 않는다
        Path base = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path target = base.resolve(filename).normalize();
        if (!target.startsWith(base) || target.equals(base)) {
            return;
        }
        File file = target.toFile();
        if (file.exists()) {
            file.delete();
        }
    }

    private static String extensionOf(String name) {
        if (name == null) return "";
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        String base = name.substring(slash + 1);
        int dot = base.lastIndexOf('.');
        return dot < 0 ? "" : base.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** 파일 앞머리(매직 넘버)로 실제 사진 형식을 본다. 확장자만 바꾼 HTML 을 막기 위함. */
    private static String sniff(MultipartFile file) throws IOException {
        byte[] h = new byte[12];
        int n;
        try (InputStream in = file.getInputStream()) {
            n = in.readNBytes(h, 0, h.length);
        }
        if (n >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "jpg";
        if (n >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "png";
        if (n >= 6 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8') return "gif";
        if (n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return "webp";
        return "";
    }
}
