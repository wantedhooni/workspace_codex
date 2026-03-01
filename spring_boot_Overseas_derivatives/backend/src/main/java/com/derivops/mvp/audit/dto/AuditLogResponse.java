package com.derivops.mvp.audit.dto;
import com.derivops.mvp.audit.*;
import com.derivops.mvp.audit.api.*;
import com.derivops.mvp.audit.application.*;
import com.derivops.mvp.audit.infrastructure.*;


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
