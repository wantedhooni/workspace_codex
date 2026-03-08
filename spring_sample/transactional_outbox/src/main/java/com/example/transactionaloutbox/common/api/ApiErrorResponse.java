package com.example.transactionaloutbox.common.api;

import java.time.Instant;

public record ApiErrorResponse(
        String message,
        Instant timestamp
) {
}
