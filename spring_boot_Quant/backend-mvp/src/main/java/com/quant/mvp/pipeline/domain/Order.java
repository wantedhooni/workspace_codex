package com.quant.mvp.pipeline.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(
        Long orderId,
        Long portfolioId,
        String symbol,
        OrderSide side,
        OrderType orderType,
        TimeInForce timeInForce,
        BigDecimal limitPrice,
        BigDecimal quantity,
        BigDecimal filledQuantity,
        OrderStatus status,
        Instant createdAt,
        String decisionReason,
        Instant decidedAt
) {
    public Order withFill(BigDecimal newFilledQuantity, OrderStatus newStatus) {
        return new Order(
                orderId,
                portfolioId,
                symbol,
                side,
                orderType,
                timeInForce,
                limitPrice,
                quantity,
                newFilledQuantity,
                newStatus,
                createdAt,
                decisionReason,
                decidedAt
        );
    }

    public Order withDecision(OrderStatus newStatus, String reason, Instant decisionTime) {
        return new Order(
                orderId,
                portfolioId,
                symbol,
                side,
                orderType,
                timeInForce,
                limitPrice,
                quantity,
                filledQuantity,
                newStatus,
                createdAt,
                reason,
                decisionTime
        );
    }
}
