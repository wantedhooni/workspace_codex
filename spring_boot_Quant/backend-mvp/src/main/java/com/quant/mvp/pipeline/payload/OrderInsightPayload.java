package com.quant.mvp.pipeline.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderInsightPayload {

    private OrderInsightPayload() {
    }

    public record TradeItem(
            Long tradeId,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal notional,
            BigDecimal fee,
            BigDecimal slippage,
            BigDecimal netCashFlow,
            Instant tradedAt
    ) {
    }

    public record AuditItem(
            Long auditId,
            String action,
            String fromStatus,
            String toStatus,
            String reason,
            String actor,
            Instant actedAt
    ) {
    }

    public record RiskItem(
            String severity,
            String code,
            String message,
            String metricName,
            BigDecimal metricValue,
            BigDecimal thresholdValue,
            Instant occurredAt
    ) {
    }

    public record Res(
            Long orderId,
            Long portfolioId,
            String symbol,
            String side,
            String orderType,
            String timeInForce,
            String status,
            BigDecimal quantity,
            BigDecimal filledQuantity,
            BigDecimal remainingQuantity,
            BigDecimal fillRatePct,
            BigDecimal requestedNotional,
            BigDecimal executedNotional,
            BigDecimal averageExecutionPrice,
            BigDecimal totalFee,
            BigDecimal totalSlippage,
            BigDecimal netCashFlow,
            Long staleMinutes,
            Boolean cancelable,
            Boolean rejectable,
            String decisionReason,
            Instant createdAt,
            Instant decidedAt,
            List<TradeItem> trades,
            List<AuditItem> audits,
            List<RiskItem> riskAlerts
    ) {
    }
}
