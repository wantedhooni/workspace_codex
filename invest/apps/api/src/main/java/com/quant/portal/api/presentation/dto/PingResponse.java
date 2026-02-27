package com.quant.portal.api.presentation.dto;

import java.time.Instant;

public record PingResponse(
        String message,
        Instant timestamp
) {
}
