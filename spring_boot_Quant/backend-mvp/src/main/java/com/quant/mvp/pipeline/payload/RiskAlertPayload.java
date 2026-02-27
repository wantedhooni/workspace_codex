package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class RiskAlertPayload {

    private RiskAlertPayload() {
    }

    public record Item(
            String alertKey,
            Long portfolioId,
            String severity,
            String code,
            String message,
            String metricName,
            BigDecimal metricValue,
            BigDecimal thresholdValue,
            Instant occurredAt,
            Boolean acknowledged,
            String acknowledgementNote,
            String acknowledgedBy,
            Instant acknowledgedAt,
            String workflowStatus,
            String assignee,
            String resolvedBy,
            Instant resolvedAt,
            String workflowUpdatedBy,
            Instant workflowUpdatedAt,
            Long ageMinutes,
            Integer slaTargetMinutes,
            Boolean slaBreached,
            Integer priorityScore
    ) {
    }

    public record OverviewItem(
            Long portfolioId,
            Long totalCount,
            Long criticalCount,
            Long warnCount,
            Long infoCount,
            Long unacknowledgedCount,
            Long openCount,
            Long inProgressCount,
            Long resolvedCount,
            Long slaBreachedCount,
            Long oldestOpenAgeMinutes,
            Long avgAckMinutes,
            Long avgResolveMinutes,
            Instant generatedAt
    ) {
    }

    public record OverviewRes(
            List<OverviewItem> items
    ) {
    }

    public record AckReq(
            @NotNull Long portfolioId,
            @NotBlank @Size(max = 120) String alertKey,
            @Size(max = 200) String note
    ) {
    }

    public record UnackReq(
            @NotNull Long portfolioId,
            @NotBlank @Size(max = 120) String alertKey
    ) {
    }

    public record AckRes(
            Item item
    ) {
    }

    public record WorkflowReq(
            @NotNull Long portfolioId,
            @NotBlank @Size(max = 120) String alertKey,
            @NotBlank @Size(max = 20) String workflowStatus,
            @Size(max = 200) String note,
            @Size(max = 120) String assignee
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
