package com.example.r2dbcauditlog.auditlog;

import jakarta.validation.constraints.NotBlank;

public record CreateApprovalRequest(
        @NotBlank String requestNumber,
        @NotBlank String requester,
        @NotBlank String targetSystem,
        @NotBlank String reason
) {
}
