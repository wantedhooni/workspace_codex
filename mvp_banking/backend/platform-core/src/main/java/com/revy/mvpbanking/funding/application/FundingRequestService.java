package com.revy.mvpbanking.funding.application;

import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.account.domain.AccountStatus;
import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import com.revy.mvpbanking.approval.domain.ApprovalRequestRepository;
import com.revy.mvpbanking.approval.domain.ApprovalStatus;
import com.revy.mvpbanking.approval.domain.ApprovalTargetType;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.customer.domain.Customer;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.funding.domain.FundingRequest;
import com.revy.mvpbanking.funding.domain.FundingRequestRepository;
import com.revy.mvpbanking.funding.domain.FundingRequestStatus;
import com.revy.mvpbanking.funding.domain.FundingRequestType;
import com.revy.mvpbanking.notification.application.NotificationService;
import com.revy.mvpbanking.notification.domain.NotificationCategory;
import com.revy.mvpbanking.notification.domain.NotificationSeverity;
import com.revy.mvpbanking.transaction.domain.TransactionEntry;
import com.revy.mvpbanking.transaction.domain.TransactionEntryRepository;
import com.revy.mvpbanking.transaction.domain.TransactionStatus;
import com.revy.mvpbanking.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class FundingRequestService {

    private final FundingRequestRepository fundingRequestRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public FundingRequestService(
            FundingRequestRepository fundingRequestRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            ApprovalRequestRepository approvalRequestRepository,
            TransactionEntryRepository transactionEntryRepository,
            AuditLogService auditLogService,
            NotificationService notificationService
    ) {
        this.fundingRequestRepository = fundingRequestRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.transactionEntryRepository = transactionEntryRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<FundingRequest> getAdminRequests() {
        auditLogService.logCurrentActor(AuditActionType.FUNDING_REQUEST_LIST_VIEWED, "FUNDING_REQUEST", "all", "Viewed funding requests");
        return fundingRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<FundingRequest> getUserRequests(UUID endUserId) {
        UUID customerId = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"))
                .getId();
        return fundingRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public FundingRequest create(UUID endUserId, UUID accountId, FundingRequestType requestType, BigDecimal amount, String note) {
        Customer customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));

        validateRequestAccount(account, customer.getId());
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Funding amount must be positive");
        }
        if (requestType == FundingRequestType.WITHDRAWAL && account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient account balance for withdrawal request");
        }

        FundingRequest request = fundingRequestRepository.save(
                new FundingRequest(
                        customer.getId(),
                        customer.getEmail(),
                        account.getId(),
                        account.getAccountNumber(),
                        account.getAccountType().name(),
                        "FND-" + System.currentTimeMillis(),
                        requestType,
                        amount,
                        account.getCurrency(),
                        account.getBalance(),
                        normalizeNote(note)
                )
        );

        approvalRequestRepository.save(
                new ApprovalRequest(
                        ApprovalTargetType.FUNDING_REQUEST,
                        request.getId(),
                        requestType.name() + " funding " + request.getRequestNumber(),
                        requestType.name() + " " + amount.toPlainString() + " " + account.getCurrency() + " for " + account.getAccountNumber(),
                        ApprovalStatus.PENDING,
                        customer.getEmail()
                )
        );

        auditLogService.logCurrentActor(
                AuditActionType.FUNDING_REQUEST_CREATED,
                "FUNDING_REQUEST",
                request.getId().toString(),
                "Created " + requestType.name() + " funding request " + request.getRequestNumber()
        );
        notificationService.notifyUser(
                "USER-FUNDING-CREATED:" + request.getId(),
                customer.getEndUserId(),
                NotificationCategory.FUNDING,
                NotificationSeverity.INFO,
                "입출금 요청 접수",
                request.getRequestNumber() + " 요청이 접수되어 운영 승인 대기열로 전달되었습니다.",
                "/funding-requests",
                "FUNDING_REQUEST",
                request.getId()
        );
        notificationService.notifyActiveAdmins(
                "ADMIN-FUNDING-PENDING:" + request.getId(),
                NotificationCategory.FUNDING,
                NotificationSeverity.ACTION_REQUIRED,
                "신규 입출금 요청",
                request.getRequestNumber() + " 요청이 승인 대기열에 추가되었습니다.",
                "/funding-requests",
                "FUNDING_REQUEST",
                request.getId()
        );
        return request;
    }

    @Transactional
    public void markApproved(UUID requestId) {
        FundingRequest request = fundingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Funding request not found"));
        if (request.getStatus() == FundingRequestStatus.APPROVED) {
            return;
        }
        if (request.getStatus() == FundingRequestStatus.REJECTED) {
            throw new ResponseStatusException(CONFLICT, "Rejected funding request cannot be approved");
        }

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
        Instant settledAt = Instant.now();
        String transactionNumber = "TXN-FND-" + request.getRequestNumber();

        try {
            if (request.getRequestType() == FundingRequestType.DEPOSIT) {
                account.credit(request.getAmount());
            } else {
                account.debit(request.getAmount());
            }
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        }

        transactionEntryRepository.findByTransactionNumber(transactionNumber)
                .orElseGet(() -> transactionEntryRepository.save(
                        new TransactionEntry(
                                account.getId(),
                                transactionNumber,
                                request.getRequestType() == FundingRequestType.DEPOSIT ? TransactionType.DEPOSIT : TransactionType.WITHDRAWAL,
                                TransactionStatus.COMPLETED,
                                request.getAmount(),
                                request.getCurrency(),
                                buildSettlementDescription(request, account),
                                settledAt
                        )
                ));
        request.approve(transactionNumber, settledAt);

        customerRepository.findById(request.getCustomerId())
                .map(Customer::getEndUserId)
                .ifPresent(endUserId -> notificationService.notifyUser(
                        "USER-FUNDING-APPROVED:" + request.getId(),
                        endUserId,
                        NotificationCategory.FUNDING,
                        NotificationSeverity.SUCCESS,
                        "입출금 정산 완료",
                        request.getRequestNumber() + " 요청이 승인되어 계좌에 반영되었습니다.",
                        "/funding-requests",
                        "FUNDING_REQUEST",
                        request.getId()
                ));
    }

    @Transactional
    public void markRejected(UUID requestId) {
        FundingRequest request = fundingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Funding request not found"));
        if (request.getStatus() == FundingRequestStatus.APPROVED) {
            throw new ResponseStatusException(CONFLICT, "Approved funding request cannot be rejected");
        }
        request.reject();
        customerRepository.findById(request.getCustomerId())
                .map(Customer::getEndUserId)
                .ifPresent(endUserId -> notificationService.notifyUser(
                        "USER-FUNDING-REJECTED:" + request.getId(),
                        endUserId,
                        NotificationCategory.FUNDING,
                        NotificationSeverity.WARNING,
                        "입출금 요청 반려",
                        request.getRequestNumber() + " 요청이 반려되었습니다. 필요 시 운영 채널에 문의해 주세요.",
                        "/funding-requests",
                        "FUNDING_REQUEST",
                        request.getId()
                ));
    }

    private void validateRequestAccount(Account account, UUID customerId) {
        if (!account.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(FORBIDDEN, "Account does not belong to current user");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(BAD_REQUEST, "Only active accounts can submit funding requests");
        }
    }

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        return note.trim();
    }

    private String buildSettlementDescription(FundingRequest request, Account account) {
        if (request.getRequestType() == FundingRequestType.DEPOSIT) {
            return "Funding deposit settled to " + account.getAccountNumber();
        }
        return "Funding withdrawal settled from " + account.getAccountNumber();
    }
}
