package com.derivops.mvp.audit.application;

public record AuditLogRequestedEvent(
        String actor,
        String action,
        String targetType,
        String targetId,
        String details
) {
}
