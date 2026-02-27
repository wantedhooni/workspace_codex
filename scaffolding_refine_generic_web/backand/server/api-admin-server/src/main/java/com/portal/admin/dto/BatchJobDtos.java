package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public final class BatchJobDtos {
    private BatchJobDtos() {}

    @Schema(name = "CreateBatchJobRequest", description = "Batch job creation payload")
    public record CreateBatchJobRequest(
            @Schema(description = "Job name", example = "Daily Report")
            @NotBlank String name,
            @Schema(description = "Job key", example = "DAILY_REPORT")
            @NotBlank String jobKey,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}

    @Schema(name = "UpdateBatchJobRequest", description = "Batch job update payload")
    public record UpdateBatchJobRequest(
            @Schema(description = "Job name")
            String name,
            @Schema(description = "Job key")
            String jobKey,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}

    @Schema(name = "BatchJobResponse", description = "Batch job response")
    public record BatchJobResponse(
            @Schema(description = "ID")
            Long id,
            @Schema(description = "Job name")
            String name,
            @Schema(description = "Job key")
            String jobKey,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}
}
