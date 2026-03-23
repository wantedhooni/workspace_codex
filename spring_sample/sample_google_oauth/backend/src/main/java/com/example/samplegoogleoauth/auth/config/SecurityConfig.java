package com.example.samplegoogleoauth.auth.config;

import com.example.samplegoogleoauth.auth.service.GoogleOAuth2FailureHandler;
import com.example.samplegoogleoauth.auth.service.GoogleOAuth2SuccessHandler;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Google OAuth 로그인과 세션 보호 API에 대한 보안 정책을 구성한다.
 */
@Configuration
public class SecurityConfig {

    /**
     * 프론트엔드와 백엔드 간 세션 기반 통신을 허용하는 보안 체인을 정의한다.
     */
    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        GoogleOAuth2SuccessHandler successHandler,
        GoogleOAuth2FailureHandler failureHandler
    ) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/logout", "/api/auth/signup", "/h2-console/**"))
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/actuator/health", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/status").permitAll()
                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout", "/api/auth/signup").authenticated()
                .requestMatchers("/api/auth/me").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .logout(logout -> logout.disable());

        return http.build();
    }

    /**
     * 로컬 프론트엔드 개발 서버에서 인증 쿠키를 포함해 API를 호출할 수 있도록 CORS를 설정한다.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(AppAuthProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(properties.frontendUrl()));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
