package com.example.samplegoogleoauth.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 발급과 만료 정책을 관리한다.
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    @NotBlank String secret,
    @Min(1) long accessTokenMinutes,
    @Min(1) long refreshTokenDays
) {
}
