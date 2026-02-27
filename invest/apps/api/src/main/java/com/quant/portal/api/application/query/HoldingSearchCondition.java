package com.quant.portal.api.application.query;

public record HoldingSearchCondition(
        Long portfolioId,
        Long instrumentId,
        String keyword
) {
}
