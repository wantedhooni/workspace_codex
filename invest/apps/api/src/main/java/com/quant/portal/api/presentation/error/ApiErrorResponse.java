package com.quant.portal.api.presentation.error;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        String code,
        String message,
        Object details,
        String traceId
) {
}
