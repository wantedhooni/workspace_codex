package com.revy.mvpbanking.admin.application;

import java.math.BigDecimal;
import java.time.Instant;

record AdminOverviewMetrics(
        long pendingApprovals,
        long overdueApprovals,
        long reviewRequiredCustomers,
        long lockedAccounts,
        long pendingFundingRequests,
        long pendingExchanges,
        long pendingStockOrders,
        long partiallyFilledOrders,
        BigDecimal pendingInstructionVolumeKrw,
        Instant latestFxEffectiveAt,
        Instant latestStockQuoteEffectiveAt,
        long freshFxPairs,
        long staleFxPairs,
        long freshStockQuotes,
        long staleStockQuotes
) {
}
