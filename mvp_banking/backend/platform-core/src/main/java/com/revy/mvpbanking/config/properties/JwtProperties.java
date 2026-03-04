package com.revy.mvpbanking.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenExpirationSeconds,
        long refreshTokenExpirationSeconds
) {
}
