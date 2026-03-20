package com.example.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 서명과 만료 정책 설정을 보관한다.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long expirationMinutes
) {
}
