package com.example.samplegoogleoauth.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Redis 연결 설정을 관리한다.
 */
@Validated
@ConfigurationProperties(prefix = "app.redisson")
public record RedissonProperties(
    @NotBlank String address,
    String password,
    @Min(1000) int connectTimeout,
    @Min(100) int operationTimeout
) {
}
