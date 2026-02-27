package com.quant.portal.api.presentation.dto.quantstrategy;

import com.quant.portal.domain.quant.enums.QuantStyle;
import com.quant.portal.domain.quant.enums.StrategyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuantStrategyCreateRequest(
        @NotBlank String name,
        @NotNull QuantStyle style,
        StrategyStatus status,
        @Positive Integer rebalanceCycleDays,
        String description
) {
}
