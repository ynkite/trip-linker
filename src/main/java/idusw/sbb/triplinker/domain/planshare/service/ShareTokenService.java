package idusw.sbb.triplinker.domain.planshare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 읽기 전용 공유 링크의 토큰.
 * 예전 토큰은 여행 번호를 고정값과 XOR 한 것이라 화면 JS 로 거꾸로 풀렸고, 번호를 1 부터 바꿔 가며 남의 일정을 읽을 수 있었다.
 * 지금은 「번호-서명」 꼴: 서명은 서버만 아는 비밀 값으로 만든 HMAC-SHA256 이라 번호를 바꾸면 맞는 서명을 만들 수 없다.
 * DB 를 바꾸지 않으려고 저장하지 않는 서명 방식으로 했다(같은 여행이면 늘 같은 토큰).
 */
@Component
public class ShareTokenService {

    private static final int SIG_HEX = 24; // 96비트 — 추측 불가, 주소 길이는 짧게
    private final byte[] secret;

    public ShareTokenService(@Value("${app.share.secret:${jwt.secret:}}") String secret) {
        if (secret == null || secret.isBlank()) {
            // 설정이 없으면(시험 등) 띄울 때마다 새 비밀 값 — 재시작하면 링크가 바뀌므로 운영에서는 jwt.secret 이 쓰인다
            byte[] r = new byte[32];
            new SecureRandom().nextBytes(r);
            this.secret = r;
        } else {
            this.secret = ("trip-share:" + secret).getBytes(StandardCharsets.UTF_8);
        }
    }

    public String tokenFor(Long tripId) {
        return tripId + "-" + sign(tripId);
    }

    /** 토큰이 이 여행 번호의 것이면 true. */
    public boolean isValid(Long tripId, String token) {
        if (tripId == null || token == null) return false;
        int dash = token.indexOf('-');
        if (dash <= 0) return false;
        if (!token.substring(0, dash).equals(String.valueOf(tripId))) return false;
        byte[] given = token.substring(dash + 1).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = sign(tripId).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(given, expected);
    }

    private String sign(Long tripId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] h = mac.doFinal(String.valueOf(tripId).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h).substring(0, SIG_HEX);
        } catch (Exception e) {
            throw new IllegalStateException("공유 토큰을 만들 수 없습니다.", e);
        }
    }
}
