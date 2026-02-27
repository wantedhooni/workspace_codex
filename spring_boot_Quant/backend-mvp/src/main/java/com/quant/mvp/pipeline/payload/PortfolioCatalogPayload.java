package com.quant.mvp.pipeline.payload;

import java.util.List;

public final class PortfolioCatalogPayload {

    private PortfolioCatalogPayload() {
    }

    public record Item(
            Long portfolioId,
            String portfolioCode,
            String portfolioName,
            String strategyTag,
            String benchmark,
            String baseCurrency,
            Boolean active
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
