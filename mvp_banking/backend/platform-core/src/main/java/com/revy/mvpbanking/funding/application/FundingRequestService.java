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
import com.revy.mvpbanking.linkedaccount.application.LinkedBankAccountService;
import com.revy.mvpbanking.common.support.MaskingUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

    private static final ZoneId OPERATIONS_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalTime FUNDING_CUTOFF_TIME = LocalTime.of(16, 0);
    private static final LocalTime PRIORITY_FUNDING_CUTOFF_TIME = LocalTime.of(18, 0);
    private static final LocalTime SAME_DAY_DEPOSIT_SETTLEMENT_TIME = LocalTime.of(17, 0);
    private static final LocalTime SAME_DAY_WITHDRAWAL_SETTLEMENT_TIME = LocalTime.of(18, 0);
    private static final LocalTime SAME_DAY_PRIORITY_DEPOSIT_SETTLEMENT_TIME = LocalTime.of(16, 30);
    private static final LocalTime SAME_DAY_PRIORITY_WITHDRAWAL_SETTLEMENT_TIME = LocalTime.of(17, 0);
    private static final LocalTime NEXT_DAY_DEPOSIT_SETTLEMENT_TIME = LocalTime.of(10, 0);
    private static final LocalTime NEXT_DAY_WITHDRAWAL_SETTLEMENT_TIME = LocalTime.of(11, 0);
    private static final BigDecimal KRW_DEPOSIT_DAILY_LIMIT = new BigDecimal("20000000.0000");
    private static final BigDecimal FX_DEPOSIT_DAILY_LIMIT = new BigDecimal("15000.0000");
    private static final BigDecimal KRW_WITHDRAWAL_DAILY_LIMIT = new BigDecimal("3000000.0000");
    private static final BigDecimal FX_WITHDRAWAL_DAILY_LIMIT = new BigDecimal("2500.0000");
    private static final BigDecimal KRW_DEPOSIT_MANUAL_REVIEW_THRESHOLD = new BigDecimal("7500000.0000");
    private static final BigDecimal FX_DEPOSIT_MANUAL_REVIEW_THRESHOLD = new BigDecimal("5000.0000");
    private static final BigDecimal KRW_WITHDRAWAL_MANUAL_REVIEW_THRESHOLD = new BigDecimal("1000000.0000");
    private static final BigDecimal FX_WITHDRAWAL_MANUAL_REVIEW_THRESHOLD = new BigDecimal("1000.0000");
    private static final BigDecimal KRW_WITHDRAWAL_SERVICE_FEE = new BigDecimal("1000.0000");
    private static final BigDecimal FX_WITHDRAWAL_SERVICE_FEE = new BigDecimal("2.5000");
    private static final BigDecimal KRW_PRIORITY_FEE = new BigDecimal("2000.0000");
    private static final BigDecimal FX_PRIORITY_FEE = new BigDecimal("3.0000");
    private static final int MAX_CANCEL_REASON_LENGTH = 255;

    private final FundingRequestRepository fundingRequestRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final LinkedBankAccountService linkedBankAccountService;

    public FundingRequestService(
            FundingRequestRepository fundingRequestRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            ApprovalRequestRepository approvalRequestRepository,
            TransactionEntryRepository transactionEntryRepository,
            AuditLogService auditLogService,
            NotificationService notificationService,
            LinkedBankAccountService linkedBankAccountService
    ) {
        this.fundingRequestRepository = fundingRequestRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.transactionEntryRepository = transactionEntryRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.linkedBankAccountService = linkedBankAccountService;
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
    public FundingRequest create(
            UUID endUserId,
            UUID accountId,
            FundingRequestType requestType,
            BigDecimal amount,
            UUID linkedBankAccountId,
            Boolean priorityProcessing,
            String note
    ) {
        Customer customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));

        validateRequestAccount(account, customer.getId());
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Funding amount must be positive");
        }
        boolean normalizedPriorityProcessing = Boolean.TRUE.equals(priorityProcessing);
        FundingSettlementSnapshot settlementSnapshot = buildSettlementSnapshot(
                requestType,
                account.getCurrency(),
                amount,
                normalizedPriorityProcessing
        );
        if (requestType == FundingRequestType.WITHDRAWAL
                && account.getBalance().compareTo(settlementSnapshot.totalDebitAmount()) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient account balance for withdrawal request including service fee");
        }
        if (requestType != FundingRequestType.WITHDRAWAL && linkedBankAccountId != null) {
            throw new ResponseStatusException(BAD_REQUEST, "Linked bank account is only allowed for withdrawal requests");
        }

        Instant requestedAt = Instant.now();
        FundingPolicyDecision policyDecision = evaluatePolicy(
                customer.getId(),
                requestType,
                account.getCurrency(),
                amount,
                requestedAt,
                normalizedPriorityProcessing
        );

        UUID targetLinkedBankAccountId = null;
        String targetBankName = null;
        String targetBankAlias = null;
        String targetBankMaskedNumber = null;
        String targetBankHolderName = null;
        if (requestType == FundingRequestType.WITHDRAWAL) {
            if (linkedBankAccountId == null) {
                throw new ResponseStatusException(BAD_REQUEST, "Withdrawal request requires linked bank account");
            }
            var linkedBankAccount = linkedBankAccountService.getOwnedActiveLinkedBankAccount(customer.getId(), linkedBankAccountId);
            targetLinkedBankAccountId = linkedBankAccount.getId();
            targetBankName = linkedBankAccount.getBankName();
            targetBankAlias = linkedBankAccount.getAccountAlias();
            targetBankMaskedNumber = MaskingUtils.maskAccountNumber(linkedBankAccount.getAccountNumber());
            targetBankHolderName = linkedBankAccount.getAccountHolderName();
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
                        settlementSnapshot.requestAmount(),
                        account.getCurrency(),
                        account.getBalance(),
                        settlementSnapshot.serviceFeeAmount(),
                        normalizedPriorityProcessing,
                        settlementSnapshot.priorityFeeAmount(),
                        settlementSnapshot.totalDebitAmount(),
                        targetLinkedBankAccountId,
                        targetBankName,
                        targetBankAlias,
                        targetBankMaskedNumber,
                        targetBankHolderName,
                        policyDecision.dailyLimitAmount(),
                        policyDecision.dailyAccumulatedAmount(),
                        policyDecision.dailyLimitExceeded(),
                        policyDecision.sameDaySettlementEligible(),
                        policyDecision.expectedSettlementAt(),
                        policyDecision.manualReviewRequired(),
                        policyDecision.manualReviewReason(),
                        normalizeNote(note)
                )
        );

        approvalRequestRepository.save(
                new ApprovalRequest(
                        ApprovalTargetType.FUNDING_REQUEST,
                        request.getId(),
                        requestType.name() + " funding " + request.getRequestNumber(),
                        buildApprovalDescription(requestType, request, account, policyDecision),
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
    public FundingRequest cancelByUser(UUID endUserId, UUID requestId, String cancelReason) {
        Customer customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        FundingRequest fundingRequest = fundingRequestRepository.findByIdAndCustomerId(requestId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Funding request not found"));

        String normalizedReason = sanitizeText(cancelReason, "Funding cancel reason", MAX_CANCEL_REASON_LENGTH);
        if (normalizedReason == null) {
            normalizedReason = "사용자 요청 취소";
        }

        try {
            fundingRequest.cancel(normalizedReason);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }

        final String decisionReason = normalizedReason;
        approvalRequestRepository.findByTargetTypeAndTargetIdAndStatus(
                        ApprovalTargetType.FUNDING_REQUEST,
                        fundingRequest.getId(),
                        ApprovalStatus.PENDING
                )
                .ifPresent(approvalRequest -> approvalRequest.cancel(
                        customer.getEmail(),
                        "사용자 취소: " + decisionReason
                ));

        auditLogService.logCurrentActor(
                AuditActionType.FUNDING_REQUEST_CANCELED,
                "FUNDING_REQUEST",
                fundingRequest.getId().toString(),
                "Canceled funding request " + fundingRequest.getRequestNumber()
        );
        notificationService.notifyActiveAdmins(
                "ADMIN-FUNDING-CANCELED:" + fundingRequest.getId(),
                NotificationCategory.FUNDING,
                NotificationSeverity.INFO,
                "사용자 입출금 요청 취소",
                fundingRequest.getRequestNumber() + " 요청이 사용자 요청으로 취소되었습니다.",
                "/funding-requests",
                "FUNDING_REQUEST",
                fundingRequest.getId()
        );
        return fundingRequest;
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
        if (request.getStatus() == FundingRequestStatus.CANCELED) {
            throw new ResponseStatusException(CONFLICT, "Canceled funding request cannot be approved");
        }

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
        Instant settledAt = Instant.now();
        String transactionNumber = "TXN-FND-" + request.getRequestNumber();

        try {
            if (request.getRequestType() == FundingRequestType.DEPOSIT) {
                account.credit(request.getAmount());
            } else {
                account.debit(request.getTotalDebitAmount());
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
                                request.getRequestType() == FundingRequestType.WITHDRAWAL
                                        ? request.getTotalDebitAmount()
                                        : request.getAmount(),
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
                        buildUserSettlementMessage(request),
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
        if (request.getStatus() == FundingRequestStatus.CANCELED) {
            throw new ResponseStatusException(CONFLICT, "Canceled funding request cannot be rejected");
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
        return sanitizeText(note, "Funding note", 255);
    }

    private String buildSettlementDescription(FundingRequest request, Account account) {
        if (request.getRequestType() == FundingRequestType.DEPOSIT) {
            return "Funding deposit settled to " + account.getAccountNumber();
        }
        if (request.getLinkedBankName() != null && request.getLinkedBankAccountNumberMasked() != null) {
            return "Funding withdrawal settled from " + account.getAccountNumber()
                    + " to " + request.getLinkedBankName()
                    + " " + request.getLinkedBankAccountNumberMasked()
                    + " (amount " + request.getAmount().toPlainString()
                    + ", fee " + request.getServiceFeeAmount().toPlainString()
                    + ", priority fee " + request.getPriorityFeeAmount().toPlainString()
                    + ", total debit " + request.getTotalDebitAmount().toPlainString() + ")";
        }
        return "Funding withdrawal settled from " + account.getAccountNumber()
                + " (amount " + request.getAmount().toPlainString()
                + ", fee " + request.getServiceFeeAmount().toPlainString()
                + ", priority fee " + request.getPriorityFeeAmount().toPlainString()
                + ", total debit " + request.getTotalDebitAmount().toPlainString() + ")";
    }

    private FundingPolicyDecision evaluatePolicy(
            UUID customerId,
            FundingRequestType requestType,
            String currency,
            BigDecimal amount,
            Instant requestedAt,
            boolean priorityProcessing
    ) {
        ZonedDateTime operationsNow = requestedAt.atZone(OPERATIONS_ZONE);
        LocalDate operationsDate = operationsNow.toLocalDate();
        Instant dayStart = operationsDate.atStartOfDay(OPERATIONS_ZONE).toInstant();
        Instant dayEnd = operationsDate.plusDays(1).atStartOfDay(OPERATIONS_ZONE).toInstant();
        BigDecimal dailyLimitAmount = resolveDailyLimitAmount(requestType, currency);
        BigDecimal manualReviewThreshold = resolveManualReviewThreshold(requestType, currency);

        BigDecimal existingDailyAmount = fundingRequestRepository.findByCustomerIdAndRequestTypeAndCreatedAtBetween(customerId, requestType, dayStart, dayEnd).stream()
                .filter(request -> request.getStatus() == FundingRequestStatus.PENDING_APPROVAL || request.getStatus() == FundingRequestStatus.APPROVED)
                .map(FundingRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dailyAccumulatedAmount = existingDailyAmount.add(amount);
        boolean dailyLimitExceeded = dailyAccumulatedAmount.compareTo(dailyLimitAmount) > 0;
        boolean sameDaySettlementEligible = resolveSameDaySettlementEligible(
                operationsDate,
                operationsNow.toLocalTime(),
                priorityProcessing
        );
        Instant expectedSettlementAt = resolveExpectedSettlementAt(
                operationsDate,
                requestType,
                sameDaySettlementEligible,
                priorityProcessing
        );

        List<String> manualReviewReasons = new ArrayList<>();
        if (amount.compareTo(manualReviewThreshold) >= 0) {
            manualReviewReasons.add(requestType == FundingRequestType.WITHDRAWAL ? "고액 출금 심사" : "고액 입금 확인");
        }
        if (dailyLimitExceeded) {
            manualReviewReasons.add("일일 한도 초과");
        }
        if (priorityProcessing) {
            manualReviewReasons.add("우선 처리 요청");
        }

        return new FundingPolicyDecision(
                dailyLimitAmount,
                dailyAccumulatedAmount,
                dailyLimitExceeded,
                sameDaySettlementEligible,
                expectedSettlementAt,
                !manualReviewReasons.isEmpty(),
                manualReviewReasons.isEmpty() ? null : String.join(" / ", manualReviewReasons)
        );
    }

    private BigDecimal resolveDailyLimitAmount(FundingRequestType requestType, String currency) {
        boolean krw = "KRW".equalsIgnoreCase(currency);
        if (requestType == FundingRequestType.WITHDRAWAL) {
            return krw ? KRW_WITHDRAWAL_DAILY_LIMIT : FX_WITHDRAWAL_DAILY_LIMIT;
        }
        return krw ? KRW_DEPOSIT_DAILY_LIMIT : FX_DEPOSIT_DAILY_LIMIT;
    }

    private BigDecimal resolveManualReviewThreshold(FundingRequestType requestType, String currency) {
        boolean krw = "KRW".equalsIgnoreCase(currency);
        if (requestType == FundingRequestType.WITHDRAWAL) {
            return krw ? KRW_WITHDRAWAL_MANUAL_REVIEW_THRESHOLD : FX_WITHDRAWAL_MANUAL_REVIEW_THRESHOLD;
        }
        return krw ? KRW_DEPOSIT_MANUAL_REVIEW_THRESHOLD : FX_DEPOSIT_MANUAL_REVIEW_THRESHOLD;
    }

    private Instant resolveExpectedSettlementAt(
            LocalDate operationsDate,
            FundingRequestType requestType,
            boolean sameDaySettlementEligible,
            boolean priorityProcessing
    ) {
        if (sameDaySettlementEligible) {
            LocalTime settlementTime = resolveSameDaySettlementTime(requestType, priorityProcessing);
            return operationsDate.atTime(settlementTime).atZone(OPERATIONS_ZONE).toInstant();
        }

        LocalDate nextBusinessDate = nextBusinessDate(operationsDate.plusDays(1));
        LocalTime settlementTime = requestType == FundingRequestType.WITHDRAWAL
                ? NEXT_DAY_WITHDRAWAL_SETTLEMENT_TIME
                : NEXT_DAY_DEPOSIT_SETTLEMENT_TIME;
        return nextBusinessDate.atTime(settlementTime).atZone(OPERATIONS_ZONE).toInstant();
    }

    private boolean resolveSameDaySettlementEligible(
            LocalDate operationsDate,
            LocalTime operationsTime,
            boolean priorityProcessing
    ) {
        if (!isBusinessDay(operationsDate)) {
            return false;
        }
        if (operationsTime.isBefore(FUNDING_CUTOFF_TIME)) {
            return true;
        }
        return priorityProcessing && operationsTime.isBefore(PRIORITY_FUNDING_CUTOFF_TIME);
    }

    private LocalTime resolveSameDaySettlementTime(FundingRequestType requestType, boolean priorityProcessing) {
        if (priorityProcessing) {
            return requestType == FundingRequestType.WITHDRAWAL
                    ? SAME_DAY_PRIORITY_WITHDRAWAL_SETTLEMENT_TIME
                    : SAME_DAY_PRIORITY_DEPOSIT_SETTLEMENT_TIME;
        }
        return requestType == FundingRequestType.WITHDRAWAL
                ? SAME_DAY_WITHDRAWAL_SETTLEMENT_TIME
                : SAME_DAY_DEPOSIT_SETTLEMENT_TIME;
    }

    private LocalDate nextBusinessDate(LocalDate candidate) {
        LocalDate next = candidate;
        while (!isBusinessDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    private boolean isBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> true;
        };
    }

    private String buildApprovalDescription(
            FundingRequestType requestType,
            FundingRequest request,
            Account account,
            FundingPolicyDecision policyDecision
    ) {
        StringBuilder description = new StringBuilder()
                .append(requestType.name())
                .append(' ')
                .append(request.getAmount().toPlainString())
                .append(' ')
                .append(account.getCurrency())
                .append(" for ")
                .append(account.getAccountNumber());
        if (requestType == FundingRequestType.WITHDRAWAL
                && request.getServiceFeeAmount().compareTo(BigDecimal.ZERO) > 0) {
            description.append(" / fee ").append(request.getServiceFeeAmount().toPlainString())
                    .append(" / priority fee ").append(request.getPriorityFeeAmount().toPlainString())
                    .append(" / total debit ").append(request.getTotalDebitAmount().toPlainString());
        }
        if (requestType == FundingRequestType.DEPOSIT
                && request.getPriorityFeeAmount().compareTo(BigDecimal.ZERO) > 0) {
            description.append(" / priority fee ").append(request.getPriorityFeeAmount().toPlainString());
        }

        if (policyDecision.manualReviewRequired()) {
            description.append(" / review: ").append(policyDecision.manualReviewReason());
        }
        if (!policyDecision.sameDaySettlementEligible()) {
            description.append(" / next settlement window");
        }
        return description.toString();
    }

    private String buildUserSettlementMessage(FundingRequest request) {
        if (request.getRequestType() == FundingRequestType.DEPOSIT) {
            return request.getRequestNumber() + " 요청이 승인되어 계좌에 반영되었습니다.";
        }
        return request.getRequestNumber() + " 요청이 승인되어 "
                + request.getTotalDebitAmount().toPlainString() + " " + request.getCurrency()
                + "가 출금 처리되었습니다. (출금액 " + request.getAmount().toPlainString()
                + " / 수수료 " + request.getServiceFeeAmount().toPlainString()
                + " / 우선처리 수수료 " + request.getPriorityFeeAmount().toPlainString() + ")";
    }

    private FundingSettlementSnapshot buildSettlementSnapshot(
            FundingRequestType requestType,
            String currency,
            BigDecimal amount,
            boolean priorityProcessing
    ) {
        BigDecimal normalizedAmount = amount.setScale(4, RoundingMode.HALF_UP);
        BigDecimal priorityFeeAmount = resolvePriorityFee(currency, requestType, priorityProcessing);
        if (requestType == FundingRequestType.WITHDRAWAL) {
            BigDecimal serviceFeeAmount = resolveWithdrawalServiceFee(currency);
            return new FundingSettlementSnapshot(
                    normalizedAmount,
                    priorityFeeAmount,
                    serviceFeeAmount,
                    normalizedAmount.add(serviceFeeAmount).add(priorityFeeAmount).setScale(4, RoundingMode.HALF_UP)
            );
        }
        return new FundingSettlementSnapshot(
                normalizedAmount,
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                normalizedAmount
        );
    }

    private BigDecimal resolveWithdrawalServiceFee(String currency) {
        boolean krw = "KRW".equalsIgnoreCase(currency);
        return krw ? KRW_WITHDRAWAL_SERVICE_FEE : FX_WITHDRAWAL_SERVICE_FEE;
    }

    private BigDecimal resolvePriorityFee(String currency, FundingRequestType requestType, boolean priorityProcessing) {
        if (!priorityProcessing || requestType != FundingRequestType.WITHDRAWAL) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        boolean krw = "KRW".equalsIgnoreCase(currency);
        return (krw ? KRW_PRIORITY_FEE : FX_PRIORITY_FEE).setScale(4, RoundingMode.HALF_UP);
    }

    private static String sanitizeText(String text, String label, int maxLength) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new ResponseStatusException(BAD_REQUEST, label + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    private record FundingPolicyDecision(
            BigDecimal dailyLimitAmount,
            BigDecimal dailyAccumulatedAmount,
            boolean dailyLimitExceeded,
            boolean sameDaySettlementEligible,
            Instant expectedSettlementAt,
            boolean manualReviewRequired,
            String manualReviewReason
    ) {
    }

    private record FundingSettlementSnapshot(
            BigDecimal requestAmount,
            BigDecimal priorityFeeAmount,
            BigDecimal serviceFeeAmount,
            BigDecimal totalDebitAmount
    ) {
    }
}
