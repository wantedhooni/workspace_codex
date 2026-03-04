package com.revy.mvpbanking.user.presentation;

import java.math.BigDecimal;
import java.util.List;

public record UserDashboardInsightResponse(
        BigDecimal totalAssetsKrw,
        BigDecimal cashAssetsKrw,
        BigDecimal investmentAssetsKrw,
        BigDecimal unrealizedProfitLossKrw,
        BigDecimal realizedProfitLossKrw,
        long activeAccountCount,
        long pendingInstructionCount,
        long partiallyFilledOrderCount,
        List<AttentionItem> attentionItems,
        List<CurrencyExposure> currencyExposures,
        List<TopPosition> topPositions
) {
    public record AttentionItem(
            String severity,
            String title,
            String message,
            String actionPath
    ) {
    }

    public record CurrencyExposure(
            String currency,
            BigDecimal cashBalance,
            BigDecimal positionValue,
            BigDecimal totalExposure,
            BigDecimal krwEquivalent
    ) {
    }

    public record TopPosition(
            String symbol,
            String market,
            BigDecimal marketValue,
            BigDecimal unrealizedProfitLoss,
            BigDecimal unrealizedProfitRate,
            String currency
    ) {
    }
}
