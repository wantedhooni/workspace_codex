package com.derivops.mvp.account.dto;
import com.derivops.mvp.account.*;
import com.derivops.mvp.account.api.*;
import com.derivops.mvp.account.application.*;
import com.derivops.mvp.account.infrastructure.*;


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
