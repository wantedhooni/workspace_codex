package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class RiskLimitPayload {

    private RiskLimitPayload() {
    }

    public record Req(
            @NotNull Long portfolioId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal maxOrderNotional,
            @NotNull @DecimalMin(value = "0.01") BigDecimal maxPositionNotionalPerSymbol,
            @NotNull @DecimalMin(value = "0.01") BigDecimal maxDailyTurnover,
            @NotNull @Min(1) Integer maxOpenOrdersPerSymbol,
            @NotNull @DecimalMin(value = "0") BigDecimal commissionBps,
            @NotNull @DecimalMin(value = "0") BigDecimal slippageBps
    ) {
    }

    public record Item(
            Long portfolioId,
            BigDecimal maxOrderNotional,
            BigDecimal maxPositionNotionalPerSymbol,
            BigDecimal maxDailyTurnover,
            Integer maxOpenOrdersPerSymbol,
            BigDecimal commissionBps,
            BigDecimal slippageBps,
            Boolean tradingEnabled,
            String killSwitchReason,
            Instant killSwitchUpdatedAt,
            String killSwitchUpdatedBy
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
