package com.example.r2dbcauditlog.auditlog;

import reactor.core.publisher.Mono;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ApprovalRequestRepository extends ReactiveCrudRepository<ApprovalRequest, Long> {

    Mono<Boolean> existsByRequestNumber(String requestNumber);

    Mono<ApprovalRequest> findByRequestNumber(String requestNumber);
}
