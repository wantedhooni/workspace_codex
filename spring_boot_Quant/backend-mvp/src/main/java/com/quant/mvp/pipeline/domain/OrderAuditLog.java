package com.quant.mvp.pipeline.domain;

import java.time.Instant;

public record OrderAuditLog(
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
