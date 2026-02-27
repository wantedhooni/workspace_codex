package com.quant.portal.api.presentation.dto.portfolio;

import com.quant.portal.domain.portfolio.enums.CurrencyCode;
import jakarta.validation.constraints.NotBlank;

public record PortfolioCreateRequest(
        @NotBlank String name,
        CurrencyCode baseCurrency
) {
}
