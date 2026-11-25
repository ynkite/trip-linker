package idusw.sbb.triplinker.config;

import idusw.sbb.triplinker.domain.auth.filter.JwtAuthenticationFilter;
import idusw.sbb.triplinker.domain.auth.security.OAuth2FailureHandler;
import idusw.sbb.triplinker.domain.auth.security.OAuth2SuccessHandler;
import idusw.sbb.triplinker.domain.auth.service.CustomOAuth2UserService;
import idusw.sbb.triplinker.domain.demo.filter.DemoGuardFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;


// Spring Security 전역 설정 클래스이다.
// 애플리케이션의 모든 보안 규칙(인증, 인가, CORS, 세션 등)을 총괄하는 코드이며
// JWT(토큰)를 사용하기 위한 기본 세팅들을 담당한다

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final DemoGuardFilter demoGuardFilter;

    // 카메라 · 마이크 · 위치 · 결제를 쓰지 않으므로 모두 끈다 (화면 코드에 geolocation 호출 0건)
    static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=(), payment=(), usb=()";

    // CSP 를 실제로 적용한다(Report-Only 로 시험한 정책 그대로 + 카카오톡 공유).
    // 운영 화면에서 강제 적용으로 바꿔 확인한 것: 지도 · 장소 찾기 · 테스트 로그인 · 커뮤니티 · 날씨 · 가계부 PDF · 카카오톡 공유가 그대로 된다.
    // - script-src 에 'unsafe-inline' 이 없다: 화면의 <script> 덩어리는 static/js/page 로 옮겼고,
    //   onclick 같은 인라인 속성은 data-onclick 으로 바꿔 inline_handlers.js 가 실행한다(eval 없이)
    // - style-src 의 'unsafe-inline' 은 남긴다: 화면이 style 속성을 많이 쓰고, 스타일은 스크립트를 실행하지 못한다
    // - 'unsafe-eval' 은 넣지 않는다: 카카오 지도 스크립트가 eval 을 한 번 시도하다 막히지만 지도는 그대로 그려진다
    // - form-action 에 카카오: 카카오톡 공유 SDK 가 sharer.kakao.com 으로 폼을 보내고 로그인(accounts.kakao.com)으로 넘긴다
    static final String CSP = String.join("; ",
            "default-src 'self'",
            "script-src 'self' 'unsafe-inline' https://dapi.kakao.com https://t1.daumcdn.net https://t1.kakaocdn.net https://cdnjs.cloudflare.com",
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
            "font-src 'self' data: https://fonts.gstatic.com",
            "img-src 'self' data: blob: https:",
            "connect-src 'self' https://dapi.kakao.com https://*.kakao.com https://*.daumcdn.net",
            "frame-src 'self' https://*.kakao.com",
            "object-src 'none'",
            "base-uri 'self'",
            "frame-ancestors 'self'",
            "form-action 'self' https://*.kakao.com");

    //비밀번호 암호화 도구 등록
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //전체적인 보안 규칙을 조립하는 핵심 부분
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 기본 보안 세팅 (프론트 허용, 안 쓰는 구식 보안/세션 끄기)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // API URL 주소별 접근 권한 나누기
                .authorizeHttpRequests(auth -> auth
                        //정적 리소스 (CSS, JS, 이미지 등) 전체 허용
                        // (전에는 "/**.css" 같은 꼴이라 스프링 경로 규칙에 맞지 않아 시험 문맥에서 설정이 뜨지 않았다. 실제 폴더 기준으로 다시 적음)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/robots.txt", "/plan", "/plan/**").permitAll()

                        // 업로드된 이미지 파일 비로그인 접근 허용
                        .requestMatchers("/uploads/**").permitAll()

                        //인증 없이 누구나 접근 가능한 공통 API 목록(비로그인)
                        .requestMatchers(
                                "/",
                                "/error",
                                "/api/auth/signup",                 //회원가입
                                "/api/auth/login",                  //로그인
                                "/api/auth/check-username",         //아이디 중복 확인
                                "/api/auth/check-email",            //이메일 중복 확인
                                "/api/auth/send-email",             //이메일 인증 발송
                                "/api/auth/verify-email",           //이메일 인증 확인
                                "/api/auth/password/reset-request", //비밀번호 재설정 요청
                                "/api/auth/password/reset",         //비밀번호 재설정 처리
                                "/api/auth/refresh",                //토큰 재발급 (접속 토큰이 만료된 뒤에 부르므로 재발급 토큰 자체로 확인)
                                "/api/auth/demo",                   //테스트 로그인 단추 (APP_DEMO_LOGIN_ENABLED)
                                "/api/maps/weather",
                                "/oauth2/authorization/**",         //소셜 로그인 요청
                                "/login/oauth2/code/**"             //소셜 로그인 콜백

                        ).permitAll()

                        // 게시판은 보는 것(GET)만 비로그인 허용
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()                        // 게시글 목록
                        .requestMatchers(HttpMethod.GET, "/api/posts/*").permitAll()               // 게시글 상세
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()      // 댓글 조회
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/place-reviews").permitAll() // 게시글별 장소 리뷰
                        .requestMatchers(HttpMethod.GET, "/api/places").permitAll()                // 장소 카드 탭 목록
                        .requestMatchers(HttpMethod.GET, "/api/places/**").permitAll()             // 장소 상세 (후기·리뷰)
                        .requestMatchers(HttpMethod.GET, "/api/trips/*").permitAll()               // 비회원 공유 링크
                        .requestMatchers(HttpMethod.GET, "/api/trips/*/routes").permitAll()        // 공유 링크 읽기 전용
                        .requestMatchers(HttpMethod.GET, "/api/trips/*/input-form").permitAll()     // 비회원 인원수/이동수단 정보
                        .requestMatchers(HttpMethod.GET, "/api/curations").permitAll()
                        // 고정 경로 모드 여부. 비로그인 화면에서도 안내 바를 띄워야 하고
                        // 내려주는 값이 on/off 와 안내 문구뿐이라 인증을 걸지 않는다.
                        .requestMatchers(HttpMethod.GET, "/api/demo-mode").permitAll()


                        // 관리자 전용 기능
                        // 테스트 관리자(DEMO_ADMIN)는 읽기만 가능. 바꾸는 요청은 DemoGuardFilter 가 403 으로 막는다.
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "DEMO_ADMIN")

                        // (Swagger 허용 줄은 뺐다 — springdoc 의존성이 없어 열려 있던 주소가 아무 데도 쓰이지 않았다)

                        // 그 외의 모든 API 요청은 반드시 인증(JWT 유효성 검증) 필요
                        .anyRequest().authenticated()
                )

                // 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                // 보안 응답 헤더 (Mozilla Observatory 에서 걸린 CSP · Referrer-Policy · Permissions-Policy)
                .headers(h -> h
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", PERMISSIONS_POLICY))
                        .addHeaderWriter(new StaticHeadersWriter("Content-Security-Policy", CSP))
                )

                // Spring Security의 기본 인증 필터가 동작하기 전에, 커스텀 JWT 필터가 먼저 토큰을 검증하도록 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 테스트 계정 제한(읽기 전용 관리자, AI 하루 횟수). JWT 필터가 인증을 채운 뒤에 돈다.
                .addFilterAfter(demoGuardFilter, JwtAuthenticationFilter.class);

        return http.build();
    }


    // 전역 CORS(교차 출처 리소스 공유) 세부 정책 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        //허용할 출처(프론트엔드 도메인) 설정 - 배포 시 특정 도메인으로 변경
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        //모든 HTTP 메서드(GET, POST, PUT, DELETE 등) 요청 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        //요청 시 전달되는 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        //프론트엔드에서 응답 헤더에 담긴 'Authorization' 값을 읽을 수 있도록 노출 설정
        configuration.addExposedHeader("Authorization");
        // 인증 정보(쿠키) 포함 요청은 허용하지 않는다. 로그인은 Authorization 헤더로 하므로 필요 없고,
        // 「모든 출처 + credentials 허용」 은 점검 도구가 늘 위험으로 거는 조합이다. (같은 도메인 화면에는 영향 없음)
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //애플리케이션의 모든 엔드포인트("/**")에 위에서 정의한 CORS 정책 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
