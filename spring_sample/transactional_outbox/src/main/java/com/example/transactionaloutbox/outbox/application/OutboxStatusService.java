package com.example.transactionaloutbox.outbox.application;

import com.example.transactionaloutbox.config.OutboxRelayProperties;
import com.example.transactionaloutbox.outbox.domain.OutboxEvent;
import com.example.transactionaloutbox.outbox.domain.OutboxEventRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox 이벤트 발행 결과에 따라 상태를 갱신한다.
 */
@Service
public class OutboxStatusService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxRelayProperties outboxRelayProperties;

    public OutboxStatusService(OutboxEventRepository outboxEventRepository, OutboxRelayProperties outboxRelayProperties) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxRelayProperties = outboxRelayProperties;
    }

    /**
     * Outbox 이벤트를 발행 완료 상태로 전환한다.
     *
     * @param outboxEventId Outbox 이벤트 식별자
     */
    @Transactional
    public void markPublished(String outboxEventId) {
        OutboxEvent event = getById(outboxEventId);
        event.markPublished(Instant.now());
    }

    /**
     * Outbox 이벤트를 재시도 대기 상태로 되돌린다.
     *
     * @param outboxEventId Outbox 이벤트 식별자
     * @param errorMessage 마지막 오류 메시지
     */
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
