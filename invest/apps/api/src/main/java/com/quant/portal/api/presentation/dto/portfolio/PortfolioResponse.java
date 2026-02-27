package com.quant.portal.api.presentation.dto.portfolio;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PortfolioResponse(
        Long id,
        String name,
        CurrencyCode baseCurrency,
        BigDecimal cashBalance,
        BigDecimal investedAmount,
        BigDecimal marketValue,
        BigDecimal eodValuationAmount,
        BigDecimal eodProfitLoss,
        BigDecimal eodReturnRate,
        LocalDate eodPriceDate,
        Integer pricedHoldings,
        Integer totalHoldings
) {
}
