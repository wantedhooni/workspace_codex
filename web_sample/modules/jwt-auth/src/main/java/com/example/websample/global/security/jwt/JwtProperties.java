package com.example.websample.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** JWT 발급과 검증에 필요한 설정값을 바인딩합니다. */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {
}
