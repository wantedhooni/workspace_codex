package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public final class CreateOrderPayload {

    private CreateOrderPayload() {
    }

    public record Req(
            @NotNull Long portfolioId,
            @NotBlank String symbol,
            @NotBlank String side,
            String orderType,
            String timeInForce,
            BigDecimal limitPrice,
            @NotNull @Positive BigDecimal quantity
    ) {
    }

    public record Res(
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
}
