package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.OrderStatus;
import java.time.Instant;
import java.util.List;

public final class OrderAuditPayload {

    private OrderAuditPayload() {
    }

    public record Item(
            Long auditId,
            Long orderId,
            Long portfolioId,
            String symbol,
            String action,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            String reason,
            String actor,
            Instant actedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
