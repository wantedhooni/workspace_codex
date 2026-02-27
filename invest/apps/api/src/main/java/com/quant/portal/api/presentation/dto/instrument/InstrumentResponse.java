package com.quant.portal.api.presentation.dto.instrument;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.MarketCode;

public record InstrumentResponse(
        Long id,
        String ticker,
        String name,
        MarketCode marketCode,
        CurrencyCode currencyCode
) {
}
