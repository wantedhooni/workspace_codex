package com.example.r2dbcauditlog.auditlog;

import reactor.core.publisher.Flux;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AuditLogRepository extends ReactiveCrudRepository<AuditLogEntry, Long> {

    Flux<AuditLogEntry> findByAggregateIdOrderByLoggedAtDesc(String aggregateId);
}
