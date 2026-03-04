package com.revy.mvpbanking.approval.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApprovalDecisionRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
