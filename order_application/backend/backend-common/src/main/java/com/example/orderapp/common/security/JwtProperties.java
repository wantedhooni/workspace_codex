package com.example.orderapp.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
    String issuer,
    String secret,
    long accessTokenSeconds
) {
}
