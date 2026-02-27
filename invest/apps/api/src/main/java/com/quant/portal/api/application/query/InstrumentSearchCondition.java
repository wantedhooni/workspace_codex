package com.quant.portal.api.application.query;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.MarketCode;

public record InstrumentSearchCondition(
        String keyword,
        MarketCode marketCode,
        CurrencyCode currencyCode
) {
}
