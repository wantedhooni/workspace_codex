package com.example.marketsignal.batch;

import java.time.OffsetDateTime;

/**
 * 배치 실행 이력 한 건을 운영 화면에서 표시하기 위한 응답이다.
 */
public record BatchExecutionResponse(
        Long executionId,
        String status,
        String exitCode,
        String exitDescription,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt
) {
}
