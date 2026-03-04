package com.example.sampleeventdriven.outbox.api;

public record OutboxPendingResponse(
        long pendingCount
) {
}
