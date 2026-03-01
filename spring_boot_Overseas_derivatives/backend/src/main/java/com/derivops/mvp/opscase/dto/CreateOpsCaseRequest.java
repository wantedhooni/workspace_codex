package com.derivops.mvp.opscase.dto;
import com.derivops.mvp.opscase.*;
import com.derivops.mvp.opscase.api.*;
import com.derivops.mvp.opscase.application.*;
import com.derivops.mvp.opscase.infrastructure.*;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateOpsCaseRequest(
        @NotNull OpsCaseCategory category,
        @NotNull OpsCaseSeverity severity,
        @NotBlank String title,
        @NotBlank String description,
        String assignee,
        OffsetDateTime dueAt,
        String linkedType,
        String linkedId,
        Long accountId
) {
}
