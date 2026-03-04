package com.revy.mvpbanking.admin.presentation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminOverviewResponse(
        Metrics metrics,
        MarketStatus marketStatus,
        List<Alert> alerts
) {
    public record Metrics(
            long pendingApprovals,
            long overdueApprovals,
            long reviewRequiredCustomers,
            long lockedAccounts,
            long pendingFundingRequests,
            long pendingExchanges,
            long pendingStockOrders,
            long partiallyFilledOrders,
            BigDecimal pendingInstructionVolumeKrw
    ) {
    }

    public record MarketStatus(
            Instant latestFxEffectiveAt,
            Instant latestStockQuoteEffectiveAt,
            long freshFxPairs,
            long staleFxPairs,
            long freshStockQuotes,
            long staleStockQuotes
    ) {
    }

    public record Alert(
            String severity,
            String title,
            String message,
            String actionPath
    ) {
    }
}
