package com.revy.mvpbanking.approval.application;

import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import com.revy.mvpbanking.approval.domain.ApprovalRequestRepository;
import com.revy.mvpbanking.exchange.application.ExchangeService;
import com.revy.mvpbanking.funding.application.FundingRequestService;
import com.revy.mvpbanking.stock.application.StockOrderService;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final CurrentPrincipalProvider currentPrincipalProvider;
    private final AuditLogService auditLogService;
    private final ExchangeService exchangeService;
    private final StockOrderService stockOrderService;
    private final FundingRequestService fundingRequestService;

    public ApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            CurrentPrincipalProvider currentPrincipalProvider,
            AuditLogService auditLogService,
            ExchangeService exchangeService,
            StockOrderService stockOrderService,
            FundingRequestService fundingRequestService
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.currentPrincipalProvider = currentPrincipalProvider;
        this.auditLogService = auditLogService;
        this.exchangeService = exchangeService;
        this.stockOrderService = stockOrderService;
        this.fundingRequestService = fundingRequestService;
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequest> getApprovalRequests() {
        auditLogService.logCurrentActor(AuditActionType.APPROVAL_LIST_VIEWED, "APPROVAL", "all", "Viewed approval queue");
        return approvalRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public ApprovalRequest approve(UUID approvalRequestId, String reason) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        ApprovalRequest approvalRequest = getApprovalRequest(approvalRequestId);
        try {
            approvalRequest.approve(principal.getUsername(), reason);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }
        applyTargetApproval(approvalRequest);

        auditLogService.logCurrentActor(
                AuditActionType.APPROVAL_APPROVED,
                "APPROVAL",
                approvalRequest.getId().toString(),
                "Approved request " + approvalRequest.getTitle()
        );
        return approvalRequest;
    }

    @Transactional
    public ApprovalRequest reject(UUID approvalRequestId, String reason) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        ApprovalRequest approvalRequest = getApprovalRequest(approvalRequestId);
        try {
            approvalRequest.reject(principal.getUsername(), reason);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }
        applyTargetRejection(approvalRequest);

        auditLogService.logCurrentActor(
                AuditActionType.APPROVAL_REJECTED,
                "APPROVAL",
                approvalRequest.getId().toString(),
                "Rejected request " + approvalRequest.getTitle()
        );
        return approvalRequest;
    }

    private ApprovalRequest getApprovalRequest(UUID approvalRequestId) {
        return approvalRequestRepository.findById(approvalRequestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Approval request not found"));
    }

    private void applyTargetApproval(ApprovalRequest approvalRequest) {
        switch (approvalRequest.getTargetType()) {
            case FUNDING_REQUEST -> fundingRequestService.markApproved(approvalRequest.getTargetId());
            case FX_EXCHANGE -> exchangeService.markApproved(approvalRequest.getTargetId());
            case STOCK_ORDER -> stockOrderService.markApproved(approvalRequest.getTargetId());
            default -> {
            }
        }
    }

    private void applyTargetRejection(ApprovalRequest approvalRequest) {
        switch (approvalRequest.getTargetType()) {
            case FUNDING_REQUEST -> fundingRequestService.markRejected(approvalRequest.getTargetId());
            case FX_EXCHANGE -> exchangeService.markRejected(approvalRequest.getTargetId());
            case STOCK_ORDER -> stockOrderService.markRejected(approvalRequest.getTargetId());
            default -> {
            }
        }
    }
}
