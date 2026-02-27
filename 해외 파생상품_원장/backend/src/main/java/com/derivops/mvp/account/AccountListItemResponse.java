package com.derivops.mvp.account;

import java.time.LocalDate;

public record AccountListItemResponse(
        Long id,
        String accountNo,
        String broker,
        AccountStatus status,
        String ownerName,
        LocalDate openedAt,
        LocalDate closedAt
) {
}
