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
public class ExchangeService {

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

    @Transactional(readOnly = true)
    public List<ExchangeRequest> getAdminRequests() {
        auditLogService.logCurrentActor(AuditActionType.EXCHANGE_REQUEST_LIST_VIEWED, "EXCHANGE_REQUEST", "all", "Viewed exchange requests");
        return exchangeRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ExchangeRequest> getUserRequests(UUID endUserId) {
        UUID customerId = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"))
                .getId();
        return exchangeRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public ExchangeRequest create(UUID endUserId, UUID sourceAccountId, UUID destinationAccountId, BigDecimal fromAmount) {
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

        var fxRate = fxRateService.getLatestRate(fromCurrency, toCurrency);
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
                        ExchangeRequestStatus.PENDING_APPROVAL
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

    @Transactional
    public void markApproved(UUID requestId) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Exchange request not found"));
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

    @Transactional
    public void markRejected(UUID requestId) {
        ExchangeRequest exchangeRequest = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Exchange request not found"));
        exchangeRequest.reject();
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
}
