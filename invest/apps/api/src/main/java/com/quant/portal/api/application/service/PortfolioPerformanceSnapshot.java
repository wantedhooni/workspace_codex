package com.quant.portal.api.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PortfolioPerformanceSnapshot(
        BigDecimal investedAmount,
        BigDecimal marketValue,
        BigDecimal eodValuationAmount,
        BigDecimal eodProfitLoss,
        BigDecimal eodReturnRate,
        LocalDate eodPriceDate,
        int pricedHoldings,
        int totalHoldings
) {
}
