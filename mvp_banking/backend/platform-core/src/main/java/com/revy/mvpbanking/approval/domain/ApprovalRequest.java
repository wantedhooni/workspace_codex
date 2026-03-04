package com.revy.mvpbanking.approval.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
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
@Table(name = "approval_requests")
public class ApprovalRequest extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 40)
    private ApprovalTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;

    @Column(name = "requested_by_email", nullable = false, length = 120)
    private String requestedByEmail;

    @Column(name = "decision_by_email", length = 120)
    private String decisionByEmail;

    @Column(name = "decision_reason", length = 255)
    private String decisionReason;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected ApprovalRequest() {
    }

    public ApprovalRequest(
            ApprovalTargetType targetType,
            UUID targetId,
            String title,
            String description,
            ApprovalStatus status,
            String requestedByEmail
    ) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.requestedByEmail = requestedByEmail;
    }

    public void approve(String approverEmail, String reason) {
        if (status != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval request is not pending");
        }
        this.status = ApprovalStatus.APPROVED;
        this.decisionByEmail = approverEmail;
        this.decisionReason = reason;
        this.decidedAt = Instant.now();
    }

    public void reject(String approverEmail, String reason) {
        if (status != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval request is not pending");
        }
        this.status = ApprovalStatus.REJECTED;
        this.decisionByEmail = approverEmail;
        this.decisionReason = reason;
        this.decidedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public ApprovalTargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public String getRequestedByEmail() {
        return requestedByEmail;
    }

    public String getDecisionByEmail() {
        return decisionByEmail;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }
}
