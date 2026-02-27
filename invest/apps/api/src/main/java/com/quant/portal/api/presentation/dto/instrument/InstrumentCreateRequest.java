package com.quant.portal.api.presentation.dto.instrument;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import com.quant.portal.domain.portfolio.enums.MarketCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InstrumentCreateRequest(
        @NotBlank String ticker,
        @NotBlank String name,
        @NotNull MarketCode marketCode,
        @NotNull CurrencyCode currencyCode
) {
}
