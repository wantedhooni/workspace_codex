package com.revy.mvpbanking.account.presentation;

import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.common.support.MaskingUtils;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountSummaryResponse(
        UUID id,
        String accountNumber,
        String accountType,
        String status,
        BigDecimal balance,
        String currency,
        UUID customerId
) {
    public static AccountSummaryResponse from(Account account) {
        return new AccountSummaryResponse(
                account.getId(),
                MaskingUtils.maskAccountNumber(account.getAccountNumber()),
                account.getAccountType().name(),
                account.getStatus().name(),
                account.getBalance(),
                account.getCurrency(),
                account.getCustomerId()
        );
    }
}
