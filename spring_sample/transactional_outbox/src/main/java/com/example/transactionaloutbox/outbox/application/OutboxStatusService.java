package com.example.transactionaloutbox.outbox.application;

import com.example.transactionaloutbox.config.OutboxRelayProperties;
import com.example.transactionaloutbox.outbox.domain.OutboxEvent;
import com.example.transactionaloutbox.outbox.domain.OutboxEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxStatusService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxRelayProperties outboxRelayProperties;

    public OutboxStatusService(OutboxEventRepository outboxEventRepository, OutboxRelayProperties outboxRelayProperties) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxRelayProperties = outboxRelayProperties;
    }

    @Transactional
    public void markPublished(String outboxEventId) {
        OutboxEvent event = getById(outboxEventId);
        event.markPublished(Instant.now());
    }

    @Transactional
    public void markFailed(String outboxEventId, String errorMessage) {
        OutboxEvent event = getById(outboxEventId);
        Instant now = Instant.now();
        event.markFailed(now, now.plus(outboxRelayProperties.retryBackoff()), errorMessage);
    }

    private OutboxEvent getById(String outboxEventId) {
        return outboxEventRepository.findById(outboxEventId)
                .orElseThrow(() -> new IllegalStateException("아웃박스 이벤트를 찾을 수 없습니다. outboxEventId=" + outboxEventId));
    }
}
