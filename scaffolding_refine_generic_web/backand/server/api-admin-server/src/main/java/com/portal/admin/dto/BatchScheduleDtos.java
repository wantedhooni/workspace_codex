package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class BatchScheduleDtos {
    private BatchScheduleDtos() {}

    @Schema(name = "CreateBatchScheduleRequest", description = "Batch schedule creation payload")
    public record CreateBatchScheduleRequest(
            @Schema(description = "Batch job ID", example = "1")
            @NotNull Long batchJobId,
            @Schema(description = "Cron expression", example = "0 0 2 * * *")
            @NotBlank String cronExpression,
            @Schema(description = "Timezone", example = "Asia/Seoul")
            @NotBlank String timezone,
            @Schema(description = "Enabled")
            Boolean enabled,
            @Schema(description = "Last run status", example = "SUCCESS")
            String lastStatus,
            @Schema(description = "Last run at", example = "2026-02-06T02:00:00Z")
            String lastRunAt
    ) {}

    @Schema(name = "UpdateBatchScheduleRequest", description = "Batch schedule update payload")
    public record UpdateBatchScheduleRequest(
            @Schema(description = "Batch job ID")
            Long batchJobId,
            @Schema(description = "Cron expression")
            String cronExpression,
            @Schema(description = "Timezone")
            String timezone,
            @Schema(description = "Enabled")
            Boolean enabled,
            @Schema(description = "Last run status")
            String lastStatus,
            @Schema(description = "Last run at")
            String lastRunAt
    ) {}

    @Schema(name = "BatchScheduleResponse", description = "Batch schedule response")
    public record BatchScheduleResponse(
            @Schema(description = "ID")
            Long id,
            @Schema(description = "Batch job ID")
            Long batchJobId,
            @Schema(description = "Cron expression")
            String cronExpression,
            @Schema(description = "Timezone")
            String timezone,
            @Schema(description = "Enabled")
            Boolean enabled,
            @Schema(description = "Last run status")
            String lastStatus,
            @Schema(description = "Last run at")
            String lastRunAt
    ) {}
}
