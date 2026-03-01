package com.derivops.mvp.cashfx.dto;
import com.derivops.mvp.cashfx.*;
import com.derivops.mvp.cashfx.api.*;
import com.derivops.mvp.cashfx.application.*;
import com.derivops.mvp.cashfx.infrastructure.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RequestResponse(
        UUID id,
        String requestType,
        RequestStatus status,
        Long accountId,
        String accountNo,
        BigDecimal amount,
        String currency,
        String fromCurrency,
        String toCurrency,
        BigDecimal exchangeRate,
        BigDecimal expectedToAmount,
        LocalDate exchangeRateDate,
        String exchangeRateSource,
        RequestPriority priority,
        LocalDate valueDate,
        boolean manualReviewRequired,
        String controlReason,
        Long controlPolicyId,
        String controlPolicySource,
        Long controlLimitPolicyId,
        String controlLimitPolicySource,
        BigDecimal projectedDailyExposure,
        OffsetDateTime slaDueAt,
        String requestedBy,
        String reviewedBy,
        String reason,
        String reviewReason,
        OffsetDateTime requestedAt,
        OffsetDateTime reviewedAt
) {
}
