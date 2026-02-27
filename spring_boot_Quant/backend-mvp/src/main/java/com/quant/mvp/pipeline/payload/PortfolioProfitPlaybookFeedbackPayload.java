package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PortfolioProfitPlaybookFeedbackPayload {

    private PortfolioProfitPlaybookFeedbackPayload() {
    }

    public static final class ListFeedback {
        private ListFeedback() {
        }

        public record Req(
                Long portfolioId,
                Integer limit
        ) {
        }

        public record Snapshot(
                Long portfolioId,
                Integer healthScore,
                String healthStatus,
                BigDecimal totalPnl,
                BigDecimal turnoverUsagePct,
                Integer openOrderCount,
                Integer criticalAlertCount,
                Integer warnAlertCount,
                Boolean tradingEnabled,
                Instant capturedAt
        ) {
        }

        public record Item(
                Long feedbackId,
                Long portfolioId,
                String actionKey,
                String actionTitle,
                String sourceTaskKey,
                String outcomeStatus,
                String reason,
                String executedBy,
                Instant executedAt,
                Snapshot beforeSnapshot,
                Snapshot afterSnapshot,
                BigDecimal deltaTotalPnl,
                BigDecimal deltaTurnoverUsagePct,
                Integer deltaOpenOrderCount,
                Integer deltaCriticalAlertCount,
                Integer deltaWarnAlertCount,
                String outcomeEvaluation
        ) {
        }

        public record Res(
                Req req,
                List<Item> items
        ) {
        }
    }
}
