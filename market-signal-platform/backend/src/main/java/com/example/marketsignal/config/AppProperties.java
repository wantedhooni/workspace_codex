package com.example.marketsignal.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 애플리케이션 사용자 정의 설정을 바인딩한다.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cors cors,
        Seed seed,
        Batch batch
) {

    /**
     * JWT 설정 값을 보관한다.
     */
    public record Jwt(
            String issuer,
            String secret,
            long accessTokenExpirationMinutes,
            long refreshTokenExpirationDays
    ) {
    }

    /**
     * CORS 허용 Origin 설정 값을 보관한다.
     */
    public record Cors(List<String> allowedOrigins) {
    }

    /**
     * 초기 시드 데이터 설정 값을 보관한다.
     */
    public record Seed(
            boolean enabled,
            boolean replaceMarketData
    ) {
    }

    /**
     * 배치 운영 설정 값을 보관한다.
     */
    public record Batch(
            Report report
    ) {
    }

    /**
     * 리포트 배치 스케줄 설정 값을 보관한다.
     */
    public record Report(
            String cron,
            String zoneId
    ) {
    }
}
