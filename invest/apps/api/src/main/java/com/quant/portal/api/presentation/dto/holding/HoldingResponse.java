package com.quant.portal.api.presentation.dto.holding;

import java.math.BigDecimal;

public record HoldingResponse(
        Long id,
        Long portfolioId,
        Long instrumentId,
        String ticker,
        BigDecimal quantity,
        BigDecimal averageCost
) {
}
