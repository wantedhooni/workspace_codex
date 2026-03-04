package com.example.r2dbcauditlog.auditlog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        String aggregateType,
        String aggregateId,
        String action,
        String actor,
        String detail,
        LocalDateTime loggedAt
) {

    static AuditLogResponse from(AuditLogEntry auditLogEntry) {
        return new AuditLogResponse(
                auditLogEntry.getAggregateType(),
                auditLogEntry.getAggregateId(),
                auditLogEntry.getAction(),
                auditLogEntry.getActor(),
                auditLogEntry.getDetail(),
                auditLogEntry.getLoggedAt()
        );
    }
}
