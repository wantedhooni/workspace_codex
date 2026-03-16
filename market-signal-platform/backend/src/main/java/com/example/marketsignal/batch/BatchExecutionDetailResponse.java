package com.example.marketsignal.batch;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.List;

/**
 * JDBC 메타데이터에서 읽은 JobExecution 상세 정보를 운영 화면에 전달한다.
 */
public record BatchExecutionDetailResponse(
        Long executionId,
        String status,
        String exitCode,
        String exitDescription,
        OffsetDateTime createdAt,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime lastUpdatedAt,
        Long durationSeconds,
        Map<String, String> parameters,
        List<BatchStepExecutionResponse> stepExecutions
) {
}
