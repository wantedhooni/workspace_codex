package com.revy.mvpbanking.exchange.application;

import com.revy.mvpbanking.account.domain.Account;
import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.account.domain.AccountStatus;
import com.revy.mvpbanking.account.domain.AccountType;
import com.revy.mvpbanking.approval.domain.ApprovalRequest;
import com.revy.mvpbanking.approval.domain.ApprovalRequestRepository;
import com.revy.mvpbanking.approval.domain.ApprovalStatus;
import com.revy.mvpbanking.approval.domain.ApprovalTargetType;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.exchange.domain.ExchangeRequest;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestRepository;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestStatus;
import com.revy.mvpbanking.fx.application.FxRateService;
import com.revy.mvpbanking.notification.application.NotificationService;
import com.revy.mvpbanking.notification.domain.NotificationCategory;
import com.revy.mvpbanking.notification.domain.NotificationSeverity;
import com.revy.mvpbanking.transaction.domain.TransactionEntry;
import com.revy.mvpbanking.transaction.domain.TransactionEntryRepository;
import com.revy.mvpbanking.transaction.domain.TransactionStatus;
import com.revy.mvpbanking.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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

/**
 * 환전 요청의 생성, 취소, 승인/반려 정산을 담당하는 애플리케이션 서비스입니다.
 * <p>
 * 요청 시점의 환율과 정책(당일 정산 가능 여부, 수동 심사 사유)을 스냅샷으로 보존해
 * 운영 화면과 감사 이력에서 동일한 판단 근거를 추적할 수 있게 합니다.
 */
@Service
public class ExchangeService {

    private static final int MAX_REQUEST_MEMO_LENGTH = 200;
    private static final int MAX_CANCEL_REASON_LENGTH = 255;
    private static final ZoneId OPERATIONS_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalTime EXCHANGE_CUTOFF_TIME = LocalTime.of(16, 0);
    private static final LocalTime SAME_DAY_SETTLEMENT_TIME = LocalTime.of(17, 30);
    private static final LocalTime NEXT_DAY_SETTLEMENT_TIME = LocalTime.of(10, 30);
    private static final BigDecimal KRW_MANUAL_REVIEW_THRESHOLD = new BigDecimal("5000000.0000");
    private static final BigDecimal FX_MANUAL_REVIEW_THRESHOLD = new BigDecimal("5000.0000");
    private static final long RATE_STALENESS_MANUAL_REVIEW_MINUTES = 30;

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final FxRateService fxRateService;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final TransactionEntryRepository transactionEntryRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public ExchangeService(
            ExchangeRequestRepository exchangeRequestRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            FxRateService fxRateService,
            ApprovalRequestRepository approvalRequestRepository,
            TransactionEntryRepository transactionEntryRepository,
            AuditLogService auditLogService,
            NotificationService notificationService
    ) {
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.fxRateService = fxRateService;
        this.approvalRequestRepository = approvalRequestRepository;
        this.transactionEntryRepository = transactionEntryRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    /**
     * 관리자 화면에서 사용할 환전 요청 목록을 최신순으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getAdminRequests() {
        auditLogService.logCurrentActor(AuditActionType.EXCHANGE_REQUEST_LIST_VIEWED, "EXCHANGE_REQUEST", "all", "Viewed exchange requests");
        return exchangeRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 현재 사용자(고객)의 환전 요청 목록을 최신순으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ExchangeRequest> getUserRequests(UUID endUserId) {
        UUID customerId = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"))
                .getId();
        return exchangeRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * 신규 환전 요청을 생성합니다.
     * <p>
     * 출금/입금 계좌 검증, 환율 조회, 정책 스냅샷 확정, 승인 큐 등록, 알림 발행을 함께 처리합니다.
     */
    @Transactional
    public ExchangeRequest create(
            UUID endUserId,
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal fromAmount,
            String requestMemo
    ) {
        var customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Source account not found"));
        Account destinationAccount = accountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Destination account not found"));

        validateOwnedBankingAccount(sourceAccount, customer.getId(), "Source");
        validateOwnedBankingAccount(destinationAccount, customer.getId(), "Destination");
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "Source and destination accounts must be different");
        }

        String fromCurrency = sourceAccount.getCurrency();
        String toCurrency = destinationAccount.getCurrency();
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            throw new ResponseStatusException(BAD_REQUEST, "Source and destination currencies must be different");
        }
        if (fromAmount == null || fromAmount.signum() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Amount must be positive");
        }
        if (sourceAccount.getBalance().compareTo(fromAmount) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient source account balance");
        }
        String sanitizedRequestMemo = sanitizeMemo(requestMemo, "Exchange request memo");

        var fxRate = fxRateService.getLatestRate(fromCurrency, toCurrency);
        Instant requestedAt = Instant.now();
        ExchangePolicyDecision policyDecision = evaluatePolicy(fromCurrency, fromAmount, requestedAt, fxRate.getEffectiveAt());
        BigDecimal convertedAmount = fromAmount.multiply(fxRate.getRate()).setScale(4, RoundingMode.HALF_UP);
        ExchangeRequest request = exchangeRequestRepository.save(
                new ExchangeRequest(
                        customer.getId(),
                        sourceAccount.getId(),
                        destinationAccount.getId(),
                        "FX-" + System.currentTimeMillis(),
                        fromCurrency,
                        toCurrency,
                        fromAmount.setScale(4, RoundingMode.HALF_UP),
                        fxRate.getRate(),
                        convertedAmount,
                        ExchangeRequestStatus.PENDING_APPROVAL,
                        sanitizedRequestMemo,
                        fxRate.getEffectiveAt(),
                        policyDecision.sameDaySettlementEligible(),
                        policyDecision.expectedSettlementAt(),
                        policyDecision.manualReviewRequired(),
                        policyDecision.manualReviewReason()
                )
        );

        approvalRequestRepository.save(
                new ApprovalRequest(
                        ApprovalTargetType.FX_EXCHANGE,
                        request.getId(),
                        "FX exchange " + request.getRequestNumber(),
                        sourceAccount.getAccountNumber() + " -> " + destinationAccount.getAccountNumber() + " exchange request",
                        ApprovalStatus.PENDING,
                        customer.getEmail()
                )
        );

        auditLogService.logCurrentActor(
                AuditActionType.EXCHANGE_REQUEST_CREATED,
                "EXCHANGE_REQUEST",
                request.getId().toString(),
                "Created exchange request " + request.getRequestNumber()
        );
        notificationService.notifyUser(
                "USER-EXCHANGE-CREATED:" + request.getId(),
                customer.getEndUserId(),
                NotificationCategory.EXCHANGE,
                NotificationSeverity.INFO,
                "환전 요청 접수",
                request.getRequestNumber() + " 요청이 접수되었고 운영 승인 대기 중입니다.",
                "/exchange-requests",
                "EXCHANGE_REQUEST",
                request.getId()
        );
        notificationService.notifyActiveAdmins(
                "ADMIN-EXCHANGE-PENDING:" + request.getId(),
                NotificationCategory.EXCHANGE,
                NotificationSeverity.ACTION_REQUIRED,
                "신규 환전 요청",
                request.getRequestNumber() + " 요청이 승인 대기열에 추가되었습니다.",
                "/approvals",
                "EXCHANGE_REQUEST",
                request.getId()
        );
        return request;
    }

    /**
     * 사용자가 대기중(PENDING_APPROVAL) 환전 요청을 취소합니다.
     */
    @Transactional
    public ExchangeRequest cancelByUser(UUID endUserId, UUID requestId, String cancelReason) {
        var customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findByIdAndCustomerId(requestId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Exchange request not found"));
        String normalizedReason = sanitizeText(cancelReason, "Exchange cancel reason", MAX_CANCEL_REASON_LENGTH);
        if (normalizedReason == null) {
            normalizedReason = "사용자 요청 취소";
        }

        try {
            exchangeRequest.cancel(normalizedReason);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }

        final String decisionReason = normalizedReason;
        approvalRequestRepository.findByTargetTypeAndTargetIdAndStatus(
                        ApprovalTargetType.FX_EXCHANGE,
                        exchangeRequest.getId(),
                        ApprovalStatus.PENDING
                )
                .ifPresent(approvalRequest -> approvalRequest.cancel(
                        customer.getEmail(),
                        "사용자 취소: " + decisionReason
                ));

        auditLogService.logCurrentActor(
                AuditActionType.EXCHANGE_REQUEST_CANCELED,
                "EXCHANGE_REQUEST",
                exchangeRequest.getId().toString(),
                "Canceled exchange request " + exchangeRequest.getRequestNumber()
        );
        notificationService.notifyActiveAdmins(
                "ADMIN-EXCHANGE-CANCELED:" + exchangeRequest.getId(),
                NotificationCategory.EXCHANGE,
                NotificationSeverity.INFO,
                "사용자 환전 요청 취소",
                exchangeRequest.getRequestNumber() + " 요청이 사용자 요청으로 취소되었습니다.",
                "/exchange-requests",
                "EXCHANGE_REQUEST",
                exchangeRequest.getId()
        );
        return exchangeRequest;
    }

    /**
     * 환전 요청을 승인하고 출금/입금 양쪽 거래 원장을 동시에 생성합니다.
     */
    @Transactional
    public void markApproved(UUID requestId) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Exchange request not found"));
        if (request.getStatus() == ExchangeRequestStatus.APPROVED) {
            return;
        }
        if (request.getStatus() == ExchangeRequestStatus.REJECTED) {
            throw new ResponseStatusException(CONFLICT, "Rejected exchange request cannot be approved");
        }
        if (request.getStatus() == ExchangeRequestStatus.CANCELED) {
            throw new ResponseStatusException(CONFLICT, "Canceled exchange request cannot be approved");
        }
        if (request.getSourceAccountId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Exchange request source account is not configured");
        }
        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Source account not found"));
        Account destinationAccount = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Destination account not found"));
        if (!sourceAccount.getCurrency().equalsIgnoreCase(request.getFromCurrency())
                || !destinationAccount.getCurrency().equalsIgnoreCase(request.getToCurrency())) {
            throw new ResponseStatusException(BAD_REQUEST, "Exchange request currency and settlement account do not match");
        }

        Instant settledAt = Instant.now();
        String sourceTransactionNumber = "TXN-FX-OUT-" + request.getRequestNumber();
        String destinationTransactionNumber = "TXN-FX-IN-" + request.getRequestNumber();

        try {
            sourceAccount.debit(request.getFromAmount());
            destinationAccount.credit(request.getNetToAmount());
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(BAD_REQUEST, exception.getMessage());
        }

        transactionEntryRepository.findByTransactionNumber(sourceTransactionNumber)
                .orElseGet(() -> transactionEntryRepository.save(
                        new TransactionEntry(
                                sourceAccount.getId(),
                                sourceTransactionNumber,
                                TransactionType.FX_EXCHANGE,
                                TransactionStatus.COMPLETED,
                                request.getFromAmount(),
                                request.getFromCurrency(),
                                request.getFromCurrency() + " debit settlement to " + destinationAccount.getAccountNumber(),
                                settledAt
                        )
                ));
        transactionEntryRepository.findByTransactionNumber(destinationTransactionNumber)
                .orElseGet(() -> transactionEntryRepository.save(
                        new TransactionEntry(
                                destinationAccount.getId(),
                                destinationTransactionNumber,
                                TransactionType.FX_EXCHANGE,
                                TransactionStatus.COMPLETED,
                                request.getNetToAmount(),
                                request.getToCurrency(),
                                request.getToCurrency() + " net credit settlement from " + sourceAccount.getAccountNumber() + " after fee " + request.getExchangeFeeAmount().toPlainString(),
                                settledAt
                        )
                ));
        request.approve(sourceTransactionNumber, destinationTransactionNumber, settledAt);
        customerRepository.findById(request.getCustomerId())
                .map(customer -> customer.getEndUserId())
                .ifPresent(endUserId -> notificationService.notifyUser(
                        "USER-EXCHANGE-APPROVED:" + request.getId(),
                        endUserId,
                        NotificationCategory.EXCHANGE,
                        NotificationSeverity.SUCCESS,
                        "환전 정산 완료",
                        request.getRequestNumber() + " 요청이 승인되어 " + request.getNetToAmount().toPlainString() + " " + request.getToCurrency() + " 입금이 완료되었습니다.",
                        "/exchange-requests",
                        "EXCHANGE_REQUEST",
                        request.getId()
                ));
    }

    /**
     * 환전 요청을 반려 처리하고 사용자에게 반려 알림을 전송합니다.
     */
    @Transactional
    public void markRejected(UUID requestId) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Exchange request not found"));
        try {
            exchangeRequest.reject();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(CONFLICT, exception.getMessage());
        }
        customerRepository.findById(exchangeRequest.getCustomerId())
                .map(customer -> customer.getEndUserId())
                .ifPresent(endUserId -> notificationService.notifyUser(
                        "USER-EXCHANGE-REJECTED:" + requestId,
                        endUserId,
                        NotificationCategory.EXCHANGE,
                        NotificationSeverity.WARNING,
                        "환전 요청 반려",
                        "환전 요청이 반려되었습니다. 상세 사유는 운영 채널에 문의해 주세요.",
                        "/exchange-requests",
                        "EXCHANGE_REQUEST",
                        requestId
                ));
    }

    /**
     * 환전 가능 계좌인지(소유권, 은행 계좌 타입, 활성 상태) 검증합니다.
     */
    private static void validateOwnedBankingAccount(Account account, UUID customerId, String accountLabel) {
        if (!account.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(FORBIDDEN, accountLabel + " account does not belong to current user");
        }
        if (account.getAccountType() != AccountType.BANKING) {
            throw new ResponseStatusException(BAD_REQUEST, "FX exchange requires banking accounts");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(BAD_REQUEST, accountLabel + " account must be active");
        }
    }

    private static String sanitizeMemo(String memo, String label) {
        return sanitizeText(memo, label, MAX_REQUEST_MEMO_LENGTH);
    }

    private static String sanitizeText(String text, String label, int maxLength) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new ResponseStatusException(BAD_REQUEST, label + " must be " + maxLength + " characters or less");
        }
        return normalized;
    }

    /**
     * 요청 시점 기준의 수동 심사 필요 여부와 예상 정산 시각을 계산합니다.
     */
    private static ExchangePolicyDecision evaluatePolicy(
            String fromCurrency,
            BigDecimal fromAmount,
            Instant requestedAt,
            Instant rateEffectiveAt
    ) {
        ZonedDateTime requestedLocal = requestedAt.atZone(OPERATIONS_ZONE);
        boolean businessDay = isBusinessDay(requestedLocal.toLocalDate());
        boolean beforeCutoff = !requestedLocal.toLocalTime().isAfter(EXCHANGE_CUTOFF_TIME);
        boolean sameDaySettlementEligible = businessDay && beforeCutoff;
        Instant expectedSettlementAt = resolveExpectedSettlementAt(requestedLocal.toLocalDate(), sameDaySettlementEligible);

        List<String> manualReviewReasons = new ArrayList<>();
        if (fromAmount.compareTo(resolveManualReviewThreshold(fromCurrency)) >= 0) {
            manualReviewReasons.add("고액 환전 심사");
        }
        long rateAgeMinutes = ChronoUnit.MINUTES.between(rateEffectiveAt, requestedAt);
        if (rateAgeMinutes >= RATE_STALENESS_MANUAL_REVIEW_MINUTES) {
            manualReviewReasons.add("시세 지연 확인 필요");
        }

        boolean manualReviewRequired = !manualReviewReasons.isEmpty();
        String manualReviewReason = manualReviewRequired ? String.join(" / ", manualReviewReasons) : null;

        return new ExchangePolicyDecision(
                sameDaySettlementEligible,
                expectedSettlementAt,
                manualReviewRequired,
                manualReviewReason
        );
    }

    /**
     * 환전 요청의 예상 정산 시각(당일/익영업일)을 반환합니다.
     */
    private static Instant resolveExpectedSettlementAt(LocalDate requestedDate, boolean sameDaySettlementEligible) {
        if (sameDaySettlementEligible) {
            return requestedDate.atTime(SAME_DAY_SETTLEMENT_TIME).atZone(OPERATIONS_ZONE).toInstant();
        }
        return nextBusinessDay(requestedDate).atTime(NEXT_DAY_SETTLEMENT_TIME).atZone(OPERATIONS_ZONE).toInstant();
    }

    private static BigDecimal resolveManualReviewThreshold(String currency) {
        return "KRW".equalsIgnoreCase(currency) ? KRW_MANUAL_REVIEW_THRESHOLD : FX_MANUAL_REVIEW_THRESHOLD;
    }

    private static LocalDate nextBusinessDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (!isBusinessDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private record ExchangePolicyDecision(
            boolean sameDaySettlementEligible,
            Instant expectedSettlementAt,
            boolean manualReviewRequired,
            String manualReviewReason
    ) {
    }
}
