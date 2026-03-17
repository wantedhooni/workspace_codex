package com.example.marketsignal.batch;

import java.time.OffsetDateTime;

/**
 * JDBC 메타데이터 기준 배치 실행 집계 정보를 운영 화면에 전달한다.
 */
public record BatchJdbcMetricsResponse(
        long totalExecutions,
        long completedExecutions,
        long failedExecutions,
        long runningExecutions,
        OffsetDateTime lastSuccessfulAt,
        OffsetDateTime lastFailedAt
) {
}
