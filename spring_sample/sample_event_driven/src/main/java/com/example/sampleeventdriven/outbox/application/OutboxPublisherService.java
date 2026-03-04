package com.example.sampleeventdriven.outbox.application;

import com.example.sampleeventdriven.config.EventFlowProperties;
import com.example.sampleeventdriven.messaging.DomainEventPublisher;
import com.example.sampleeventdriven.outbox.domain.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final EventFlowProperties eventFlowProperties;

    public OutboxPublisherService(
            OutboxEventRepository outboxEventRepository,
            DomainEventPublisher domainEventPublisher,
            EventFlowProperties eventFlowProperties
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.domainEventPublisher = domainEventPublisher;
        this.eventFlowProperties = eventFlowProperties;
    }

    @Transactional
    public int publishPendingEvents() {
        var events = outboxEventRepository.findTop50ByPublishedFalseOrderByIdAsc();
        int published = 0;

        for (var event : events.stream().limit(eventFlowProperties.getPublishBatchSize()).toList()) {
            domainEventPublisher.publish(event.getTopic(), event.getAggregateId(), event.getPayload());
            event.markPublished();
            outboxEventRepository.save(event);
            published++;
        }

        return published;
    }
}
