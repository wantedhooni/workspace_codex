package com.derivops.mvp.stockrecommendation.application;

import com.derivops.mvp.account.AccountStatus;
import com.derivops.mvp.stockrecommendation.RecommendationHorizon;
import com.derivops.mvp.stockrecommendation.RecommendationRiskProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StockRecommendationPromptContext(
        Long accountId,
        String accountNo,
        String broker,
        AccountStatus accountStatus,
        String ownerName,
        LocalDate cashSnapshotDate,
        RecommendationRiskProfile riskProfile,
        RecommendationHorizon investmentHorizon,
        int maxRecommendations,
        List<String> preferredMarkets,
        List<String> candidateSymbols,
        String operatorNote,
        List<CashBalanceItem> cashBalances,
        List<HoldingItem> holdings,
        List<RecentPurchaseItem> recentPurchases
) {

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

    public record RecentPurchaseItem(
            String symbol,
            String market,
            String currency,
            LocalDate tradeDate,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal netAmount
    ) {
    }
}
