package com.example.multitenancy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 발급 및 검증에 필요한 설정 값을 보관한다.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long expirationMinutes
) {
}
