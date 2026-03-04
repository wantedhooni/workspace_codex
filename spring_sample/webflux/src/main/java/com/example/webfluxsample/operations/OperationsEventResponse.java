package com.example.webfluxsample.operations;

import java.time.OffsetDateTime;

public record OperationsEventResponse(
        String domain,
        String level,
        String message,
        OffsetDateTime occurredAt
) {
}
