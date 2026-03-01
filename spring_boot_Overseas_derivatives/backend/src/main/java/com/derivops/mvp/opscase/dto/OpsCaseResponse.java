package com.derivops.mvp.opscase.dto;
import com.derivops.mvp.opscase.*;
import com.derivops.mvp.opscase.api.*;
import com.derivops.mvp.opscase.application.*;
import com.derivops.mvp.opscase.infrastructure.*;


import java.time.OffsetDateTime;

public record OpsCaseResponse(
        Long id,
        String caseNo,
        OpsCaseCategory category,
        OpsCaseSeverity severity,
        OpsCaseStatus status,
        String title,
        String description,
        String assignee,
        OffsetDateTime dueAt,
        String linkedType,
        String linkedId,
        Long accountId,
        String resolutionSummary,
        String createdBy,
        String updatedBy,
        String resolvedBy,
        OffsetDateTime resolvedAt,
        String closedBy,
        OffsetDateTime closedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
