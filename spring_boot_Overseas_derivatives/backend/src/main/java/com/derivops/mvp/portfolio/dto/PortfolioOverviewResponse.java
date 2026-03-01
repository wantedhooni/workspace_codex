package com.derivops.mvp.portfolio.dto;

import com.derivops.mvp.account.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PortfolioOverviewResponse(
        Long accountId,
        String accountNo,
        String broker,
        AccountStatus status,
        String ownerName,
        LocalDate cashSnapshotDate,
        int holdingCount,
        int recentPurchaseCount,
        List<CashBalanceItem> cashBalances,
        List<StockCostSummaryItem> stockCostByCurrency,
        List<HoldingItem> holdings,
        List<RecentPurchaseItem> recentPurchases
) {

    public record CashBalanceItem(
            String currency,
            BigDecimal amount
    ) {
    }

    public record StockCostSummaryItem(
            String currency,
            BigDecimal totalCost
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
            Long id,
            String symbol,
            String market,
            String currency,
            LocalDate tradeDate,
            LocalDate settlementDate,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal feeAmount,
            BigDecimal netAmount
    ) {
    }
}
