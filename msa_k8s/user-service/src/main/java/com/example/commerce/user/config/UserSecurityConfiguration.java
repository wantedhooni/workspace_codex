package com.example.commerce.user.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.example.commerce.security.KeycloakJwtAuthenticationConverters;
import com.example.commerce.security.OAuth2SecurityProperties;

/**
 * User Service를 Keycloak JWT 기반 무상태 Resource Server로 구성한다.
 */
@Configuration
@EnableConfigurationProperties(OAuth2SecurityProperties.class)
public class UserSecurityConfiguration {

    /**
     * User Service 보안 구성을 생성한다.
     */
    public UserSecurityConfiguration() {
    }

    /**
     * 상태 확인과 관측성 엔드포인트는 공개하고 회원 API에는 회원 또는 관리자 role을 요구한다.
     *
     * @param http Servlet HTTP 보안 구성기
     * @param properties OAuth2 활성화 설정
     * @return User Service 보안 필터 체인
     * @throws Exception Spring Security 구성 실패
     */
    @Bean
    SecurityFilterChain userSecurityFilterChain(
            HttpSecurity http,
            OAuth2SecurityProperties properties) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (properties.enabled()) {
            http.authorizeHttpRequests(requests -> requests
                            .requestMatchers(
                                    "/actuator/health/**",
                                    "/actuator/info",
                                    "/actuator/prometheus").permitAll()
                            .requestMatchers("/api/v1/users/**")
                            .hasAnyRole("commerce-user", "commerce-admin")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                            KeycloakJwtAuthenticationConverters.servlet(properties.clientId()))));
        } else {
            http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll());
        }
        return http.build();
    }
}
