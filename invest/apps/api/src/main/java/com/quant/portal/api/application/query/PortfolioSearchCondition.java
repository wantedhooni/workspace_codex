package com.quant.portal.api.application.query;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;

public record PortfolioSearchCondition(
        String keyword,
        CurrencyCode baseCurrency
) {
}
