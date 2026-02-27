package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderListPayload {

    private OrderListPayload() {
    }

    public record Item(
            Long orderId,
            Long portfolioId,
            String symbol,
            String side,
            String orderType,
            String timeInForce,
            BigDecimal limitPrice,
            BigDecimal quantity,
            BigDecimal filledQuantity,
            BigDecimal remainingQuantity,
            BigDecimal fillRate,
            OrderStatus status,
            Instant createdAt,
            String decisionReason,
            Instant decidedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
