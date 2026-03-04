package com.example.r2dbcauditlog.auditlog;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("approval_request")
public class ApprovalRequest {

    @Id
    private Long id;
    private String requestNumber;
    private String requester;
    private String targetSystem;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApprovalRequest() {
    }

    public ApprovalRequest(
            Long id,
            String requestNumber,
            String requester,
            String targetSystem,
            String status,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.requestNumber = requestNumber;
        this.requester = requester;
        this.targetSystem = targetSystem;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ApprovalRequest create(CreateApprovalRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return new ApprovalRequest(
                null,
                request.requestNumber(),
                request.requester(),
                request.targetSystem(),
                ApprovalStatus.REQUESTED.name(),
                request.reason(),
                now,
                now
        );
    }

    public ApprovalRequest approve() {
        return new ApprovalRequest(
                id,
                requestNumber,
                requester,
                targetSystem,
                ApprovalStatus.APPROVED.name(),
                reason,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Long getId() {
        return id;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public String getRequester() {
        return requester;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
