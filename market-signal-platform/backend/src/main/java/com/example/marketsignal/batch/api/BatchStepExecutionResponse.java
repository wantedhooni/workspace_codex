package com.example.marketsignal.batch;

import java.time.OffsetDateTime;

/**
 * JDBC 메타데이터에서 읽은 Step 실행 상세 정보를 운영 화면에 전달한다.
 */
public record BatchStepExecutionResponse(
        Long stepExecutionId,
        String stepName,
        String status,
        String exitCode,
        String exitDescription,
        Long readCount,
        Long writeCount,
        Long commitCount,
        Long rollbackCount,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime lastUpdatedAt
) {
}
