package com.example.r2dbcauditlog.auditlog;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ApprovalWorkflowController {

    private final ApprovalWorkflowService approvalWorkflowService;

    public ApprovalWorkflowController(ApprovalWorkflowService approvalWorkflowService) {
        this.approvalWorkflowService = approvalWorkflowService;
    }

    @PostMapping("/approval-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApprovalRequestResponse> createApprovalRequest(@Valid @RequestBody CreateApprovalRequest request) {
        return approvalWorkflowService.createApprovalRequest(request);
    }

    @PostMapping("/approval-requests/{requestNumber}/approve")
    public Mono<ApprovalRequestResponse> approveRequest(
            @PathVariable String requestNumber,
            @Valid @RequestBody ApproveRequestCommand command
    ) {
        return approvalWorkflowService.approveRequest(requestNumber, command);
    }

    @GetMapping("/approval-requests/{requestNumber}")
    public Mono<ApprovalRequestResponse> getRequest(@PathVariable String requestNumber) {
        return approvalWorkflowService.getRequest(requestNumber);
    }

    @GetMapping("/audit-logs/{aggregateId}")
    public Flux<AuditLogResponse> getAuditLogs(@PathVariable String aggregateId) {
        return approvalWorkflowService.getAuditLogs(aggregateId);
    }
}
