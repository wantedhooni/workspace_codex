package com.derivops.mvp.audit;

import java.time.OffsetDateTime;

public record AuditLogResponse(
        Long id,
        String actor,
        String action,
        String targetType,
        String targetId,
        String details,
        OffsetDateTime createdAt
) {
}
