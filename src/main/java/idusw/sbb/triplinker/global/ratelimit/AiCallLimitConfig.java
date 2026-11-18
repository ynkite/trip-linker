package idusw.sbb.triplinker.global.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** AI 호출 주소, 로그인한 상태에서 비밀번호를 확인하는 주소에 횟수 제한을 건다. */
@Configuration
@RequiredArgsConstructor
public class AiCallLimitConfig implements WebMvcConfigurer {

    private final AiCallLimitInterceptor aiCallLimitInterceptor;
    private final PasswordCheckLimitInterceptor passwordCheckLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(aiCallLimitInterceptor)
                .addPathPatterns("/api/chat/message", "/api/trips/*/routes/*");
        registry.addInterceptor(passwordCheckLimitInterceptor)
                .addPathPatterns("/api/users/me", "/api/users/me/verify-password", "/api/users/me/password");
    }
}
