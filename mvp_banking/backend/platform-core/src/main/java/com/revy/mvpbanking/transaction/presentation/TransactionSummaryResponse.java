package com.revy.mvpbanking.transaction.presentation;

import com.revy.mvpbanking.transaction.domain.TransactionEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionSummaryResponse(
        UUID id,
        UUID accountId,
        String transactionNumber,
        String transactionType,
        String status,
        BigDecimal amount,
        String currency,
        String description,
        Instant occurredAt
) {
    public static TransactionSummaryResponse from(TransactionEntry transactionEntry) {
        return new TransactionSummaryResponse(
                transactionEntry.getId(),
                transactionEntry.getAccountId(),
                transactionEntry.getTransactionNumber(),
                transactionEntry.getTransactionType().name(),
                transactionEntry.getStatus().name(),
                transactionEntry.getAmount(),
                transactionEntry.getCurrency(),
                transactionEntry.getDescription(),
                transactionEntry.getOccurredAt()
        );
    }
}
