package com.quant.portal.api.application.query;

import com.quant.portal.domain.quant.enums.QuantStyle;
import com.quant.portal.domain.quant.enums.StrategyStatus;

public record QuantStrategySearchCondition(
        String keyword,
        QuantStyle style,
        StrategyStatus status
) {
}
