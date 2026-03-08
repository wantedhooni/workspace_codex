package com.example.transactionaloutbox.outbox.domain;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findTop100ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            Instant nextAttemptAt
    );

    List<OutboxEvent> findTop50ByOrderByCreatedAtDesc();
}
