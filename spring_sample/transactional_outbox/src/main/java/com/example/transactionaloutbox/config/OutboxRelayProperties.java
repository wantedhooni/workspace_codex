package com.example.transactionaloutbox.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox")
public record OutboxRelayProperties(
        Duration fixedDelay,
        int batchSize,
        Duration publishTimeout,
        Duration retryBackoff
) {
}
