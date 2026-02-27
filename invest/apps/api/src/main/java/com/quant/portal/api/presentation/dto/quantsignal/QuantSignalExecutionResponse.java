package com.quant.portal.api.presentation.dto.quantsignal;

import java.time.Instant;

public record QuantSignalExecutionResponse(
        Long id,
        Long signalId,
        Long transactionId,
        Instant executedAt,
        String executedBy
) {
}
