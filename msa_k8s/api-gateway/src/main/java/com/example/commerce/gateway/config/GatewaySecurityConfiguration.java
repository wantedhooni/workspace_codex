package com.example.commerce.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.example.commerce.security.KeycloakJwtAuthenticationConverters;
import com.example.commerce.security.OAuth2SecurityProperties;

/**
 * Gateway를 기본 비인증 개발 모드, Keycloak JWT Resource Server, OAuth2 Client 로그인 모드로 구성한다.
 */
@Configuration
@EnableConfigurationProperties(OAuth2SecurityProperties.class)
public class GatewaySecurityConfiguration {

    /**
     * Gateway 보안 구성을 생성한다.
     */
    public GatewaySecurityConfiguration() {
    }

    /**
     * 상태 확인과 OAuth2 로그인 엔드포인트는 공개하고 업무 API에는 Keycloak 인증과 role을 요구한다.
     *
     * @param http Reactive HTTP 보안 구성기
     * @param properties OAuth2 활성화 설정
     * @return Gateway 보안 필터 체인
     */
    @Bean
    SecurityWebFilterChain gatewaySecurityWebFilterChain(
            ServerHttpSecurity http,
            OAuth2SecurityProperties properties) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable);

        if (properties.enabled() || properties.clientEnabled()) {
            http.authorizeExchange(exchanges -> exchanges
                            .pathMatchers(
                                    "/actuator/health/**",
                                    "/actuator/info",
                                    "/actuator/prometheus",
                                    "/fallback/**",
                                    "/login/**",
                                    "/oauth2/**").permitAll()
                            .pathMatchers("/api/v1/users/**")
                            .hasAnyRole("commerce-user", "commerce-admin")
                            .pathMatchers("/api/v1/accounts/**")
                            .hasAnyRole("commerce-account", "commerce-admin")
                            .pathMatchers("/api/v1/contents/**")
                            .hasAnyRole("commerce-contents", "commerce-contents-editor", "commerce-admin")
                            .anyExchange().authenticated());
            if (properties.enabled()) {
                http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                        KeycloakJwtAuthenticationConverters.reactive(properties.clientId()))));
            }
            if (properties.clientEnabled()) {
                http.oauth2Login(oauth2 -> {
                });
                http.logout(logout -> logout.logoutUrl("/logout"));
            }
        } else {
            http.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
        }
        return http.build();
    }
}
