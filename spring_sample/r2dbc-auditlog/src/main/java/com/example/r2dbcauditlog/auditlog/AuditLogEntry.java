package com.example.r2dbcauditlog.auditlog;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("audit_log")
public class AuditLogEntry {

    @Id
    private Long id;
    private String aggregateType;
    private String aggregateId;
    private String action;
    private String actor;
    private String detail;
    private LocalDateTime loggedAt;

    public AuditLogEntry() {
    }

    public AuditLogEntry(
            Long id,
            String aggregateType,
            String aggregateId,
            String action,
            String actor,
            String detail,
            LocalDateTime loggedAt
    ) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.action = action;
        this.actor = actor;
        this.detail = detail;
        this.loggedAt = loggedAt;
    }

    public static AuditLogEntry of(String aggregateId, String action, String actor, String detail) {
        return new AuditLogEntry(null, "APPROVAL_REQUEST", aggregateId, action, actor, detail, LocalDateTime.now());
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAction() {
        return action;
    }

    public String getActor() {
        return actor;
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }
}
