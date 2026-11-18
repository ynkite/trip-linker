package idusw.sbb.triplinker.config;

import idusw.sbb.triplinker.domain.demo.DemoLoginProperties;
import idusw.sbb.triplinker.domain.demo.filter.DemoGuardFilter;
import idusw.sbb.triplinker.domain.demo.service.DemoAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 테스트 로그인 설정.
 *  - 앱이 뜰 때 테스트 계정 두 개(demo_user, demo_admin)가 없으면 만든다(켜져 있을 때만).
 *    실패해도 앱 기동은 막지 않는다. 단추를 처음 누를 때 다시 만든다.
 *  - DemoGuardFilter 는 SecurityConfig 의 필터 체인 안에서만 돌게 한다
 *    (서블릿 필터로 따로 등록되면 인증 전에 먼저 돌아 아무것도 막지 못한다).
 */
@Configuration
public class DemoLoginConfig {

    private static final Logger log = LoggerFactory.getLogger(DemoLoginConfig.class);

    @Bean
    public ApplicationRunner demoAccountRunner(DemoLoginProperties properties, DemoAccountService accountService) {
        return args -> {
            if (!properties.isEnabled()) return;
            try {
                accountService.getOrCreateUser();
                accountService.getOrCreateAdmin();
                log.info("테스트 로그인 계정을 확인했습니다.");
            } catch (Exception e) {
                log.warn("테스트 로그인 계정 준비 실패(단추를 누를 때 다시 시도합니다): {}", e.getMessage());
            }
        };
    }

    @Bean
    public FilterRegistrationBean<DemoGuardFilter> demoGuardFilterRegistration(DemoGuardFilter filter) {
        FilterRegistrationBean<DemoGuardFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }
}
