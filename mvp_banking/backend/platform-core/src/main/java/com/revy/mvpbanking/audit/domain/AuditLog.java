package com.revy.mvpbanking.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 30)
    private AuditActorType actorType;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_email", length = 120)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 60)
    private AuditActionType actionType;

    @Column(name = "target_type", nullable = false, length = 80)
    private String targetType;

    @Column(name = "target_id", length = 120)
    private String targetId;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;

    protected AuditLog() {
    }

    public AuditLog(
            AuditActorType actorType,
            UUID actorId,
            String actorEmail,
            AuditActionType actionType,
            String targetType,
            String targetId,
            String description,
            Instant loggedAt
    ) {
        this.actorType = actorType;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
        this.loggedAt = loggedAt;
    }

    public UUID getId() {
        return id;
    }

    public AuditActorType getActorType() {
        return actorType;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public AuditActionType getActionType() {
        return actionType;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getLoggedAt() {
        return loggedAt;
    }
}
