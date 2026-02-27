package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class PortfolioProfitPlaybookPayload {

    private PortfolioProfitPlaybookPayload() {
    }

    public record Action(
            String actionKey,
            String severity,
            Boolean blocker,
            String title,
            String description,
            String expectedImpact,
            String ownerRole,
            String horizon,
            String path
    ) {
    }

    public record Item(
            Long portfolioId,
            String objective,
            String objectiveDetail,
            String strategyFocus,
            String marketRegime,
            String executionGuideline,
            Boolean tradable,
            Integer priorityScore,
            Integer blockerCount,
            List<Action> actions,
            Instant generatedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
