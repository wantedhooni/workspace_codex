package com.example.sampleeventdriven.outbox.api;

public record OutboxPublishResponse(
        int publishedCount
) {
}
