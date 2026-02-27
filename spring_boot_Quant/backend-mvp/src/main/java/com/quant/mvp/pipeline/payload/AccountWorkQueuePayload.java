package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class AccountWorkQueuePayload {

    private AccountWorkQueuePayload() {
    }

    public record Req(
            Long portfolioId,
            Integer staleMinutes,
            Integer topN
    ) {
    }

    public record Summary(
            Long portfolioId,
            Integer openOrderCount,
            Integer staleOrderCount,
            Integer criticalRiskCount,
            Integer warningRiskCount,
            Integer approvedVoucherCount,
            Integer draftVoucherCount,
            Integer activeSessionCount
    ) {
    }

    public record AlertItem(
            String severity,
            String message
    ) {
    }

    public record TaskItem(
            String taskKey,
            String title,
            String description,
            String severity,
            Integer count,
            String actionLabel,
            String path,
            Boolean actionEnabled
    ) {
    }

    public record Res(
            Req req,
            Summary summary,
            List<AlertItem> alerts,
            List<TaskItem> tasks,
            Instant generatedAt
    ) {
    }
}
