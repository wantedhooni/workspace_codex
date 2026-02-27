package com.derivops.mvp.batch;

import java.time.OffsetDateTime;

public record BatchRunResponse(
        Long id,
        String batchName,
        BatchStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        String errorMessage,
        int retryCount
) {
}
