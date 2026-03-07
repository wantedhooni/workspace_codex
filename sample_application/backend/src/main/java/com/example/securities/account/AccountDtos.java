package com.example.securities.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDtos {

    public record OpenAccountRequest(String customerId) {
    }

    public record AccountResponse(
            String id,
            String accountNo,
            String customerId,
            AccountStatus status,
            BigDecimal balance,
            LocalDateTime createdAt
    ) {
    }

    public record TransactionRequest(LedgerType type, BigDecimal amount, String referenceId) {
    }

    public record TransactionResponse(
            String ledgerId,
            String accountId,
            LedgerType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String referenceId,
            LocalDateTime occurredAt
    ) {
    }

    public record LedgerSearchRequest(
            String accountId,
            String customerId,
            LedgerType type,
            LocalDateTime fromOccurredAt,
            LocalDateTime toOccurredAt,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Integer limit
    ) {
    }

    public record LedgerEntryView(
            String ledgerId,
            String accountId,
            String accountNo,
            String customerId,
            LedgerType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String referenceId,
            LocalDateTime occurredAt
    ) {
    }
}
