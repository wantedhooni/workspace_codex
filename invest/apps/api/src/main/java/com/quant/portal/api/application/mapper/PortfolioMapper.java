package com.quant.portal.api.application.mapper;

import com.quant.portal.api.application.service.PortfolioPerformanceSnapshot;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioCreateRequest;
import com.quant.portal.api.presentation.dto.portfolio.PortfolioResponse;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import com.quant.portal.domain.portfolio.enums.CurrencyCode;

public final class PortfolioMapper {

    private PortfolioMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static Portfolio toEntity(PortfolioCreateRequest request) {
        if (request == null) {
            return null;
        }
        CurrencyCode baseCurrency = request.baseCurrency() == null ? CurrencyCode.KRW : request.baseCurrency();
        return new Portfolio(request.name(), baseCurrency);
    }

    public static PortfolioResponse toDto(Portfolio portfolio) {
        return toDto(portfolio, null);
    }

    public static PortfolioResponse toDto(Portfolio portfolio, PortfolioPerformanceSnapshot performance) {
        if (portfolio == null) {
            return null;
        }

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getBaseCurrency(),
                portfolio.getCashBalance(),
                performance == null ? null : performance.investedAmount(),
                performance == null ? null : performance.marketValue(),
                performance == null ? null : performance.eodValuationAmount(),
                performance == null ? null : performance.eodProfitLoss(),
                performance == null ? null : performance.eodReturnRate(),
                performance == null ? null : performance.eodPriceDate(),
                performance == null ? null : performance.pricedHoldings(),
                performance == null ? null : performance.totalHoldings()
        );
    }
}
