package com.quant.portal.api.presentation.dto.portfolio;

import jakarta.validation.constraints.NotBlank;

public record PortfolioUpdateRequest(
        @NotBlank String name
) {
}
