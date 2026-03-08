package com.example.transactionaloutbox.outbox.application;

import com.example.transactionaloutbox.config.OutboxRelayProperties;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxClaimService outboxClaimService;
    private final OutboxStatusService outboxStatusService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxRelayProperties outboxRelayProperties;

    public OutboxRelayScheduler(
            OutboxClaimService outboxClaimService,
            OutboxStatusService outboxStatusService,
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxRelayProperties outboxRelayProperties
    ) {
        this.outboxClaimService = outboxClaimService;
        this.outboxStatusService = outboxStatusService;
        this.kafkaTemplate = kafkaTemplate;
        this.outboxRelayProperties = outboxRelayProperties;
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay:3000ms}")
    public void publishPendingEvents() {
        List<ClaimedOutboxEvent> claimedEvents = outboxClaimService.claimBatch();
        for (ClaimedOutboxEvent claimedEvent : claimedEvents) {
            publish(claimedEvent);
        }
    }

    private void publish(ClaimedOutboxEvent claimedEvent) {
        try {
            kafkaTemplate.send(claimedEvent.topic(), claimedEvent.aggregateId(), claimedEvent.payload())
                    .get(outboxRelayProperties.publishTimeout().toMillis(), TimeUnit.MILLISECONDS);
            outboxStatusService.markPublished(claimedEvent.outboxEventId());
        } catch (Exception exception) {
            log.warn("아웃박스 이벤트 Kafka 발행 실패. outboxEventId={}", claimedEvent.outboxEventId(), exception);
            outboxStatusService.markFailed(claimedEvent.outboxEventId(), exception.getMessage());
        }
    }
}
