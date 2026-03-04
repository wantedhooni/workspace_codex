package com.revy.mvpbanking.audit.presentation;

import com.revy.mvpbanking.audit.domain.AuditLog;
import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String actorType,
        UUID actorId,
        String actorEmail,
        String actionType,
        String targetType,
        String targetId,
        String description,
        Instant loggedAt
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorType().name(),
                auditLog.getActorId(),
                auditLog.getActorEmail(),
                auditLog.getActionType().name(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getDescription(),
                auditLog.getLoggedAt()
        );
    }
}
