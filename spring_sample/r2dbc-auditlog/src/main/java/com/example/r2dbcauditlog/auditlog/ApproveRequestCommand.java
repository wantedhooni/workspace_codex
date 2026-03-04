package com.example.r2dbcauditlog.auditlog;

import jakarta.validation.constraints.NotBlank;

public record ApproveRequestCommand(
        @NotBlank String actor,
        @NotBlank String comment
) {
}
