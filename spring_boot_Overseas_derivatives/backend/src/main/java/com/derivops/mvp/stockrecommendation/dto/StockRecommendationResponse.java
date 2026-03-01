package com.derivops.mvp.stockrecommendation.dto;

import com.derivops.mvp.stockrecommendation.RecommendationAction;
import com.derivops.mvp.stockrecommendation.RecommendationConfidence;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record StockRecommendationResponse(
        Long accountId,
        String accountNo,
        String provider,
        String model,
        OffsetDateTime generatedAt,
        String summary,
        List<String> cautionPoints,
        List<RecommendationItem> recommendations,
        PortfolioSnapshot portfolioSnapshot,
        String disclaimer
) {
    public record RecommendationItem(
            int rank,
            String symbol,
            String market,
            RecommendationAction action,
            RecommendationConfidence confidence,
            String allocationHint,
            String rationale,
            String riskNotes
    ) {
    }

    public record PortfolioSnapshot(
            LocalDate cashSnapshotDate,
            List<CashBalanceItem> cashBalances,
            List<HoldingItem> holdings
    ) {
    }

    public record CashBalanceItem(
            String currency,
            BigDecimal amount
    ) {
    }

    public record HoldingItem(
            String symbol,
            String market,
            String currency,
            BigDecimal quantity,
            BigDecimal averagePrice,
            BigDecimal totalCost,
            LocalDate lastTradeDate
    ) {
    }
}
