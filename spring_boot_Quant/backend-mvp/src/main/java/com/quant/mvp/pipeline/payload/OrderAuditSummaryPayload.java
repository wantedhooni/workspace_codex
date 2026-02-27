package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class OrderAuditSummaryPayload {

    private OrderAuditSummaryPayload() {
    }

    public record ActionCounter(
            String action,
            Long count
    ) {
    }

    public record TransitionCounter(
            String fromStatus,
            String toStatus,
            Long count
    ) {
    }

    public record ActorCounter(
            String actor,
            Long count
    ) {
    }

    public record Res(
            Long portfolioId,
            Integer recentMinutes,
            Long totalCount,
            Long recentCount,
            Long distinctOrderCount,
            Instant lastActedAt,
            Instant generatedAt,
            List<ActionCounter> actionCounters,
            List<TransitionCounter> transitionCounters,
            List<ActorCounter> topActors
    ) {
    }
}
