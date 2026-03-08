package com.example.transactionaloutbox.outbox.application;

import com.example.transactionaloutbox.outbox.api.OutboxEventResponse;
import com.example.transactionaloutbox.outbox.domain.OutboxEvent;
import com.example.transactionaloutbox.outbox.domain.OutboxEventRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxQueryService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxQueryService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional(readOnly = true)
    public List<OutboxEventResponse> getRecentEvents() {
        return outboxEventRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    private OutboxEventResponse toResponse(OutboxEvent event) {
        return new OutboxEventResponse(
                event.getId(),
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType(),
                event.getTopic(),
                event.getStatus(),
                event.getAttemptCount(),
                event.getNextAttemptAt(),
                event.getPublishedAt(),
                event.getLastErrorMessage(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
