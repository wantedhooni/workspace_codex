package com.derivops.mvp.cashfx;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RequestResponse(
        UUID id,
        String requestType,
        RequestStatus status,
        Long accountId,
        BigDecimal amount,
        String requestedBy,
        String reviewedBy,
        String reason,
        String reviewReason,
        OffsetDateTime requestedAt,
        OffsetDateTime reviewedAt
) {
}
