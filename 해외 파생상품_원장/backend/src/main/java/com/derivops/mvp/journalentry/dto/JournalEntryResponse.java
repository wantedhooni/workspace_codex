package com.derivops.mvp.journalentry.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record JournalEntryResponse(
        Long id,
        String journalNo,
        Integer lineNo,
        Long accountId,
        String accountNo,
        String referenceType,
        String referenceId,
        LocalDate postingDate,
        String accountCode,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        String currency,
        String description,
        OffsetDateTime createdAt
) {
}
