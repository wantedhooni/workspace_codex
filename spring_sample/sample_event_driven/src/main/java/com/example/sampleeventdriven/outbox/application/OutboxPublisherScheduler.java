package com.example.sampleeventdriven.outbox.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.outbox.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisherScheduler {

    private final OutboxPublisherService outboxPublisherService;

    public OutboxPublisherScheduler(OutboxPublisherService outboxPublisherService) {
        this.outboxPublisherService = outboxPublisherService;
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay:5000}")
    public void publish() {
        outboxPublisherService.publishPendingEvents();
    }
}
