package com.derivops.mvp.batch.dto;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.application.*;
import com.derivops.mvp.batch.infrastructure.*;
import com.derivops.mvp.batch.config.*;
import com.derivops.mvp.batch.job.*;


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
