package com.example.r2dbcauditlog.auditlog;

import java.time.LocalDateTime;

public record ApprovalRequestResponse(
        String requestNumber,
        String requester,
        String targetSystem,
        ApprovalStatus status,
        String reason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    static ApprovalRequestResponse from(ApprovalRequest approvalRequest) {
        return new ApprovalRequestResponse(
                approvalRequest.getRequestNumber(),
                approvalRequest.getRequester(),
                approvalRequest.getTargetSystem(),
                ApprovalStatus.valueOf(approvalRequest.getStatus()),
                approvalRequest.getReason(),
                approvalRequest.getCreatedAt(),
                approvalRequest.getUpdatedAt()
        );
    }
}
