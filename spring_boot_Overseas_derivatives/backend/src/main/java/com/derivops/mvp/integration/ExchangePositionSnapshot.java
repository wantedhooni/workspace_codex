package com.derivops.mvp.integration;

import java.math.BigDecimal;

public record ExchangePositionSnapshot(
        String symbol,
        BigDecimal quantity,
        BigDecimal avgPrice
) {
}
