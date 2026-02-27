package com.derivops.mvp.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AccountSummaryResponse(
        Long accountId,
        String accountNo,
        String broker,
        AccountStatus status,
        LocalDate snapshotDate,
        List<BalanceItem> balances,
        List<PositionItem> positions,
        MarginItem margin
) {
    public record BalanceItem(String currency, BigDecimal amount) {}

    public record PositionItem(String symbol, BigDecimal quantity, BigDecimal avgPrice) {}

    public record MarginItem(BigDecimal initialMargin, BigDecimal maintenanceMargin, BigDecimal availableMargin) {}
}
