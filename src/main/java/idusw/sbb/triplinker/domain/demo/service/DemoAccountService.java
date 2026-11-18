package idusw.sbb.triplinker.domain.demo.service;

import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import idusw.sbb.triplinker.domain.user.entity.User;
import idusw.sbb.triplinker.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 테스트 계정 두 개(demo_user, demo_admin)를 만들고 알아보는 일을 맡는다.
 *
 * 기존 user01, admin 계정은 쓰지 않는다.
 * 비밀번호는 아무도 모르는 무작위 값을 해시해서 넣으므로 아이디/비밀번호 로그인으로는 들어올 수 없고
 * 테스트 로그인 단추(/api/auth/demo)로만 들어온다.
 */
@Service
@RequiredArgsConstructor
public class DemoAccountService {

    static final String USER_NAME   = "테스트 유저";
    static final String ADMIN_NAME  = "테스트 관리자";
    static final String USER_EMAIL  = "demo_user@example.com";
    static final String ADMIN_EMAIL = "demo_admin@example.com";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;

    // SecurityConfig 의 passwordEncoder 빈을 받지 않는다. 이 서비스가 보안 필터(DemoGuardFilter)를 거쳐
    // SecurityConfig 에 다시 물려 순환 참조가 되기 때문. 여기서 해시하는 것은 아무도 모르는 무작위 값뿐이다.
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 테스트 계정 id. 요청마다 DB를 보지 않도록 들고 있는다. */
    private final Set<Long> demoUserIds = ConcurrentHashMap.newKeySet();

    /** 테스트 유저 계정. 없으면 만든다. */
    @Transactional
    public User getOrCreateUser() {
        return getOrCreate(DemoLoginProperties.USER_USERNAME, USER_NAME, USER_EMAIL, "USER");
    }

    /** 테스트 관리자 계정. 없으면 만든다. */
    @Transactional
    public User getOrCreateAdmin() {
        return getOrCreate(DemoLoginProperties.ADMIN_USERNAME, ADMIN_NAME, ADMIN_EMAIL,
                DemoLoginProperties.ROLE_DEMO_ADMIN);
    }

    /** 이 id가 테스트 계정(유저·관리자)인지. */
    public boolean isDemoAccount(Long userId) {
        if (userId == null) return false;
        if (demoUserIds.contains(userId)) return true;
        // 서버가 다시 떠서 캐시가 비어 있을 수 있으니 한 번만 DB로 확인한다.
        return userRepository.findById(userId)
                .filter(u -> isDemoUsername(u.getUsername()))
                .map(u -> { demoUserIds.add(u.getId()); return true; })
                .orElse(false);
    }

    public static boolean isDemoUsername(String username) {
        return DemoLoginProperties.USER_USERNAME.equals(username)
                || DemoLoginProperties.ADMIN_USERNAME.equals(username);
    }

    private User getOrCreate(String username, String name, String email, String role) {
        User user = userRepository.findByUsername(username).orElseGet(() ->
                userRepository.save(User.builder()
                        .username(username)
                        .passwordHash(passwordEncoder.encode(randomSecret()))
                        .name(name)
                        .email(email)
                        .region("서울")
                        .role(role)
                        .status("ACTIVE")
                        .lastPwChangedAt(LocalDateTime.now())
                        .build()));
        demoUserIds.add(user.getId());
        return user;
    }

    /** 아무도 모르는 값의 해시. 비밀번호로는 들어올 수 없는 계정을 만들 때 쓴다(후기 전용 테스트 계정). */
    static String unusablePasswordHash() {
        return new BCryptPasswordEncoder().encode(randomSecret());
    }

    /** 화면·응답·로그 어디에도 남기지 않는 일회용 값. */
    private static String randomSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
