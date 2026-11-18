package idusw.sbb.triplinker.domain.demo.service;

import idusw.sbb.triplinker.domain.auth.dto.TokenResponseDto;
import idusw.sbb.triplinker.domain.auth.entity.RefreshToken;
import idusw.sbb.triplinker.domain.auth.repository.RefreshTokenRepository;
import idusw.sbb.triplinker.domain.auth.service.JwtProvider;
import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import idusw.sbb.triplinker.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 테스트 로그인 단추 처리.
 *
 * 토큰은 일반 로그인(AuthServiceImpl.login)과 같은 방식으로 만든다.
 * access/refresh 토큰을 내주고 refresh 토큰은 해시로만 저장한다.
 * 다른 점은 하나: 같은 계정을 여러 사람이 동시에 쓰므로 기존 refresh 토큰을 지우지 않는다.
 */
@Service
@RequiredArgsConstructor
public class DemoLoginService {

    private static final Logger log = LoggerFactory.getLogger(DemoLoginService.class);

    public enum Kind { USER, ADMIN }

    private final DemoAccountService accountService;
    private final DemoSeedService seedService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /** 되돌리기는 한 번에 하나만. 이미 누가 되돌리는 중이면 기다리지 않고 그 결과를 쓴다. */
    private final ReentrantLock resetLock = new ReentrantLock();

    public TokenResponseDto login(Kind kind) {
        User user;
        String role;
        if (kind == Kind.ADMIN) {
            user = accountService.getOrCreateAdmin();
            role = DemoLoginProperties.ROLE_DEMO_ADMIN;
            seedService.restoreAccount(user.getId(), role, DemoAccountService.ADMIN_NAME);
        } else {
            user = accountService.getOrCreateUser();
            role = "USER";
            seedService.restoreAccount(user.getId(), role, DemoAccountService.USER_NAME);
            resetQuietly(user.getId());
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), role);
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashSha256(refreshToken))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build());

        return new TokenResponseDto(accessToken, refreshToken, false);
    }

    /** 실패해도 로그인은 계속한다(되돌리기 전 데이터가 남아 있을 뿐). */
    boolean resetQuietly(Long userId) {
        if (!resetLock.tryLock()) {
            log.info("테스트 유저 되돌리기가 이미 진행 중이라 이번 요청은 건너뜁니다.");
            return false;
        }
        try {
            seedService.reset(userId, LocalDate.now());
            return true;
        } catch (Exception e) {
            log.warn("테스트 유저 되돌리기 실패, 로그인은 계속합니다: {}", e.getMessage());
            return false;
        } finally {
            resetLock.unlock();
        }
    }

    private static String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("토큰 해싱 중 오류가 발생했습니다.", e);
        }
    }
}
