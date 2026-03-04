package com.example.r2dbcauditlog.auditlog;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

@Service
public class ApprovalWorkflowService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final AuditLogRepository auditLogRepository;
    private final TransactionalOperator transactionalOperator;

    public ApprovalWorkflowService(
            ApprovalRequestRepository approvalRequestRepository,
            AuditLogRepository auditLogRepository,
            TransactionalOperator transactionalOperator
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.auditLogRepository = auditLogRepository;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<ApprovalRequestResponse> createApprovalRequest(CreateApprovalRequest request) {
        Mono<ApprovalRequestResponse> operation = approvalRequestRepository.existsByRequestNumber(request.requestNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateRequestNumberException(request.requestNumber()));
                    }

                    ApprovalRequest approvalRequest = ApprovalRequest.create(request);
                    return approvalRequestRepository.save(approvalRequest)
                            .flatMap(saved -> auditLogRepository.save(
                                            AuditLogEntry.of(saved.getRequestNumber(), "REQUEST_CREATED", saved.getRequester(), saved.getReason())
                                    )
                                    .thenReturn(saved));
                })
                .map(ApprovalRequestResponse::from);

        return transactionalOperator.transactional(operation);
    }

    public Mono<ApprovalRequestResponse> approveRequest(String requestNumber, ApproveRequestCommand command) {
        Mono<ApprovalRequestResponse> operation = approvalRequestRepository.findByRequestNumber(requestNumber)
                .switchIfEmpty(Mono.error(new ApprovalRequestNotFoundException(requestNumber)))
                .flatMap(approvalRequest -> approvalRequestRepository.save(approvalRequest.approve())
                        .flatMap(saved -> auditLogRepository.save(
                                        AuditLogEntry.of(saved.getRequestNumber(), "REQUEST_APPROVED", command.actor(), command.comment())
                                )
                                .thenReturn(saved)))
                .map(ApprovalRequestResponse::from);

        return transactionalOperator.transactional(operation);
    }

    public Mono<ApprovalRequestResponse> getRequest(String requestNumber) {
        return approvalRequestRepository.findByRequestNumber(requestNumber)
                .switchIfEmpty(Mono.error(new ApprovalRequestNotFoundException(requestNumber)))
                .map(ApprovalRequestResponse::from);
    }

    public Flux<AuditLogResponse> getAuditLogs(String aggregateId) {
        return auditLogRepository.findByAggregateIdOrderByLoggedAtDesc(aggregateId)
                .map(AuditLogResponse::from);
    }
}
