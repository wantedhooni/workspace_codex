package com.example.samplegoogleoauth.auth.config;

import com.example.samplegoogleoauth.auth.security.JwtAuthenticationFilter;
import com.example.samplegoogleoauth.auth.security.RedisOAuth2AuthorizationRequestRepository;
import com.example.samplegoogleoauth.auth.service.GoogleOAuth2FailureHandler;
import com.example.samplegoogleoauth.auth.service.GoogleOAuth2SuccessHandler;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Redis 기반 OAuth 상태 저장과 JWT 인증을 사용하는 stateless 보안 정책을 구성한다.
 */
@Configuration
public class SecurityConfig {

    /**
     * 세션 없이 OAuth 로그인과 Bearer JWT 인증을 처리하는 보안 체인을 정의한다.
     */
    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        RedisOAuth2AuthorizationRequestRepository authorizationRequestRepository,
        GoogleOAuth2SuccessHandler successHandler,
        GoogleOAuth2FailureHandler failureHandler
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(securityContext -> securityContext.securityContextRepository(new NullSecurityContextRepository()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/actuator/health", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/status").permitAll()
                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/logout", "/api/auth/signup").authenticated()
                .requestMatchers("/api/auth/me").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                .authorizationEndpoint(endpoint -> endpoint.authorizationRequestRepository(authorizationRequestRepository))
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .logout(logout -> logout.disable());

        return http.build();
    }

    /**
     * 로컬 프론트엔드 개발 서버에서 Bearer 토큰 기반 API 호출을 허용하도록 CORS를 설정한다.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(AppAuthProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(properties.frontendUrl()));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
