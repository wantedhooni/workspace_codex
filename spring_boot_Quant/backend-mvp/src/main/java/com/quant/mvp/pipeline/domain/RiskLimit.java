package com.quant.mvp.pipeline.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record RiskLimit(
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
