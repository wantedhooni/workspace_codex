package com.quant.portal.api.presentation.dto.quantstrategy;

import com.quant.portal.domain.quant.enums.QuantStyle;
import com.quant.portal.domain.quant.enums.StrategyStatus;

public record QuantStrategyResponse(
        Long id,
        String name,
        QuantStyle style,
        StrategyStatus status,
        Integer rebalanceCycleDays,
        String description
) {
}
