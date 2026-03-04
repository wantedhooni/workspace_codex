package com.revy.mvpbanking.approval.presentation;

import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import java.time.Instant;
import java.util.UUID;

public record ApprovalRequestResponse(
        UUID id,
        String targetType,
        UUID targetId,
        String title,
        String description,
        String status,
        String requestedByEmail,
        String decisionByEmail,
        String decisionReason,
        Instant decidedAt
) {
    public static ApprovalRequestResponse from(ApprovalRequest approvalRequest) {
        return new ApprovalRequestResponse(
                approvalRequest.getId(),
                approvalRequest.getTargetType().name(),
                approvalRequest.getTargetId(),
                approvalRequest.getTitle(),
                approvalRequest.getDescription(),
                approvalRequest.getStatus().name(),
                approvalRequest.getRequestedByEmail(),
                approvalRequest.getDecisionByEmail(),
                approvalRequest.getDecisionReason(),
                approvalRequest.getDecidedAt()
        );
    }
}
