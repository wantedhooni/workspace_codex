package com.example.transactionaloutbox.outbox.application;

import com.example.transactionaloutbox.config.OutboxRelayProperties;
import com.example.transactionaloutbox.outbox.domain.OutboxEvent;
import com.example.transactionaloutbox.outbox.domain.OutboxEventRepository;
import com.example.transactionaloutbox.outbox.domain.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 발행 가능한 Outbox 이벤트를 조회하고 선점 상태로 전환한다.
 */
@Service
public class OutboxClaimService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxRelayProperties outboxRelayProperties;

    public OutboxClaimService(OutboxEventRepository outboxEventRepository, OutboxRelayProperties outboxRelayProperties) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxRelayProperties = outboxRelayProperties;
    }

    /**
     * 발행 대상 이벤트를 배치로 선점한다.
     *
     * @return 선점된 Outbox 이벤트 목록
     */
    @Transactional
    public List<ClaimedOutboxEvent> claimBatch() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(OutboxStatus.PENDING, Instant.now());

        return pendingEvents.stream()
                .limit(outboxRelayProperties.batchSize())
                .peek(event -> event.markProcessing(Instant.now()))
                .map(event -> new ClaimedOutboxEvent(
                        event.getId(),
                        event.getAggregateId(),
                        event.getTopic(),
                        event.getPayload()
                ))
                .toList();
    }
}
