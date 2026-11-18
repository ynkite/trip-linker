package idusw.sbb.triplinker.domain.demo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 테스트 로그인(유저/관리자 단추) 설정.
 *
 * 환경변수로만 바꾼다. 운영 설정 파일에 아무것도 없어도 아래 기본값으로 동작한다.
 *   APP_DEMO_LOGIN_ENABLED             true/false. 기본 true. false면 단추가 숨고 /api/auth/demo 는 404.
 *   APP_DEMO_LOGIN_AI_DAILY_LIMIT      테스트 계정 하나가 하루에 부를 수 있는 AI 기능 횟수. 기본 10.
 *   APP_DEMO_LOGIN_PER_MINUTE_LIMIT    한 IP가 1분에 테스트 로그인을 누를 수 있는 횟수. 기본 10.
 *
 * 고정 경로 모드(app.demo.route.*)와는 별개다.
 */
@Getter
@Component
public class DemoLoginProperties {

    public static final String USER_USERNAME  = "demo_user";
    public static final String ADMIN_USERNAME = "demo_admin";

    /** 테스트 관리자 권한. 관리자 화면은 읽을 수 있고 바꾸는 요청은 막힌다. */
    public static final String ROLE_DEMO_ADMIN = "DEMO_ADMIN";

    public static final String READ_ONLY_MESSAGE = "테스트 계정은 바꿀 수 없습니다";

    private final boolean enabled;
    private final int aiDailyLimit;
    private final int perMinuteLimit;

    public DemoLoginProperties(
            @Value("${app.demo.login.enabled:true}") boolean enabled,
            @Value("${app.demo.login.ai-daily-limit:10}") int aiDailyLimit,
            @Value("${app.demo.login.per-minute-limit:10}") int perMinuteLimit) {
        this.enabled = enabled;
        this.aiDailyLimit = aiDailyLimit;
        this.perMinuteLimit = perMinuteLimit;
    }
}
