package idusw.sbb.triplinker.global.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** AI 호출 주소에만 횟수 제한을 건다. */
@Configuration
@RequiredArgsConstructor
public class AiCallLimitConfig implements WebMvcConfigurer {

    private final AiCallLimitInterceptor aiCallLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(aiCallLimitInterceptor)
                .addPathPatterns("/api/chat/message", "/api/trips/*/routes/*");
    }
}
