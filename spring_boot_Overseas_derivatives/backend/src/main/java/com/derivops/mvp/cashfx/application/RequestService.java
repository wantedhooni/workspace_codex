package com.derivops.mvp.cashfx.application;
import com.derivops.mvp.cashfx.*;
import com.derivops.mvp.cashfx.api.*;
import com.derivops.mvp.cashfx.dto.*;
import com.derivops.mvp.cashfx.infrastructure.*;


import com.derivops.mvp.account.Account;
import com.derivops.mvp.account.infrastructure.AccountRepository;
import com.derivops.mvp.account.AccountStatus;
import com.derivops.mvp.approval.ApprovalDomain;
import com.derivops.mvp.approval.application.ApprovalPolicyService;
import com.derivops.mvp.audit.application.AuditLogService;
import com.derivops.mvp.common.BadRequestException;
import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.exchangerate.application.ExchangeRateService;
import com.derivops.mvp.exchangerate.dto.ExchangeRateQuoteResponse;
import com.derivops.mvp.integration.BrokerAdapter;
import com.derivops.mvp.opscase.application.OpsCaseService;
import com.derivops.mvp.position.Balance;
import com.derivops.mvp.position.infrastructure.BalanceRepository;
import com.derivops.mvp.risk.application.RiskLimitPolicyService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RequestService {
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "KRW", "JPY", "GBP", "CHF", "SGD", "HKD");
    private static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(10);
    private static final int MIN_REASON_LENGTH = 8;
    private static final int MIN_REVIEW_REASON_LENGTH = 10;
    private static final int MIN_MANUAL_REVIEW_REASON_LENGTH = 15;

    private static final Map<String, BrokerCutoff> BROKER_CUTOFFS = Map.of(
            "CME", new BrokerCutoff(ZoneId.of("America/Chicago"), LocalTime.of(16, 0)),
            "EUREX", new BrokerCutoff(ZoneId.of("Europe/Berlin"), LocalTime.of(17, 30)),
            "SGX", new BrokerCutoff(ZoneId.of("Asia/Singapore"), LocalTime.of(17, 0))
    );
    private static final BrokerCutoff DEFAULT_CUTOFF = new BrokerCutoff(ZoneId.of("UTC"), LocalTime.of(17, 0));

    private final CashRequestRepository cashRequestRepository;
    private final FxRequestRepository fxRequestRepository;
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final BrokerAdapter brokerAdapter;
    private final AuditLogService auditLogService;
    private final ApprovalPolicyService approvalPolicyService;
    private final RiskLimitPolicyService riskLimitPolicyService;
    private final OpsCaseService opsCaseService;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public RequestResponse createCashRequest(CreateCashRequest request, String actor) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found: " + request.accountId()));
        ensureAccountOperable(account);
        validateReason(request.reason(), "Request reason");

        String currency = normalizeCurrency(request.currency());
        validateCurrency(currency);
        if (request.type() == CashRequestType.WITHDRAW) {
            ensureSufficientBalance(account.getId(), currency, request.amount(), "withdraw");
        }
        ensureNoDuplicatePendingCash(account.getId(), request.type(), currency, request.amount());

        LocalDate valueDate = resolveValueDate(account.getBroker(), request.valueDate());
        ApprovalDomain domain = request.type() == CashRequestType.WITHDRAW ? ApprovalDomain.CASH_WITHDRAW : ApprovalDomain.CASH_DEPOSIT;
        ApprovalPolicyService.EffectiveApprovalPolicy policy = approvalPolicyService.resolve(account.getBroker(), domain, valueDate);
        BigDecimal exposureBefore = cashRequestRepository.sumExposure(account.getId(), request.type(), currency, valueDate);
        RiskLimitPolicyService.RiskLimitEvaluation risk = riskLimitPolicyService.evaluate(
                account.getBroker(),
                domain,
                currency,
                valueDate,
                request.amount(),
                exposureBefore
        );
        String approvalControlReason = buildCashControlReason(request.type(), request.amount(), valueDate, policy);
        String riskControlReason = buildRiskControlReason(risk);
        String controlReason = mergeControlReasons(approvalControlReason, riskControlReason);
        RequestPriority priority = resolvePriority(
                request.priority(),
                request.amount(),
                controlReason != null,
                policy.highThreshold(),
                policy.urgentThreshold()
        );

        CashRequest entity = new CashRequest();
        entity.setAccount(account);
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setCurrency(currency);
        entity.setStatus(RequestStatus.PENDING);
        entity.setPriority(priority);
        entity.setValueDate(valueDate);
        entity.setManualReviewRequired(controlReason != null);
        entity.setControlReason(controlReason);
        entity.setControlPolicyId(policy.policyId());
        entity.setControlPolicySource(policy.sourceBrokerCode());
        entity.setControlLimitPolicyId(risk.policyId());
        entity.setControlLimitPolicySource(formatLimitPolicySource(risk.sourceBrokerCode(), risk.sourceCurrencyCode()));
        entity.setProjectedDailyExposure(risk.projectedExposure());
        entity.setSlaDueAt(calculateSlaDueAt(priority));
        entity.setReason(request.reason().trim());
        entity.setRequestedBy(actor);

        entity = cashRequestRepository.save(entity);
        auditLogService.log(
                actor,
                "CREATE_CASH_REQUEST",
                "CASH_REQUEST",
                entity.getId().toString(),
                "type=%s amount=%s currency=%s priority=%s valueDate=%s approvalPolicy=%s riskPolicy=%s projectedExposure=%s".formatted(
                        entity.getType(),
                        entity.getAmount(),
                        entity.getCurrency(),
                        entity.getPriority(),
                        entity.getValueDate(),
                        entity.getControlPolicyId(),
                        entity.getControlLimitPolicyId(),
                        entity.getProjectedDailyExposure()
                )
        );
        return toResponse(entity);
    }

    @Transactional
    public RequestResponse createFxRequest(CreateFxRequest request, String actor) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found: " + request.accountId()));
        ensureAccountOperable(account);
        validateReason(request.reason(), "Request reason");

        String fromCurrency = normalizeCurrency(request.fromCurrency());
        String toCurrency = normalizeCurrency(request.toCurrency());
        validateCurrency(fromCurrency);
        validateCurrency(toCurrency);
        if (fromCurrency.equals(toCurrency)) {
            throw new BadRequestException("fromCurrency and toCurrency must be different");
        }
        ensureSufficientBalance(account.getId(), fromCurrency, request.amount(), "fx");
        ensureNoDuplicatePendingFx(account.getId(), fromCurrency, toCurrency, request.amount());

        LocalDate valueDate = resolveValueDate(account.getBroker(), request.valueDate());
        ExchangeRateQuoteResponse quote = exchangeRateService.quote(fromCurrency, toCurrency, request.amount(), valueDate);
        ApprovalPolicyService.EffectiveApprovalPolicy policy = approvalPolicyService.resolve(account.getBroker(), ApprovalDomain.FX, valueDate);
        BigDecimal exposureBefore = fxRequestRepository.sumExposure(account.getId(), fromCurrency, valueDate);
        RiskLimitPolicyService.RiskLimitEvaluation risk = riskLimitPolicyService.evaluate(
                account.getBroker(),
                ApprovalDomain.FX,
                fromCurrency,
                valueDate,
                request.amount(),
                exposureBefore
        );
        String approvalControlReason = buildFxControlReason(request.amount(), fromCurrency, toCurrency, valueDate, policy);
        String riskControlReason = buildRiskControlReason(risk);
        String controlReason = mergeControlReasons(approvalControlReason, riskControlReason);
        RequestPriority priority = resolvePriority(
                request.priority(),
                request.amount(),
                controlReason != null,
                policy.highThreshold(),
                policy.urgentThreshold()
        );

        FxRequest entity = new FxRequest();
        entity.setAccount(account);
        entity.setFromCurrency(fromCurrency);
        entity.setToCurrency(toCurrency);
        entity.setAmount(request.amount());
        entity.setExchangeRate(quote.exchangeRate());
        entity.setExpectedToAmount(quote.convertedAmount());
        entity.setExchangeRateDate(quote.rateDate());
        entity.setExchangeRateSource(quote.source() + " (" + quote.quoteMode() + ")");
        entity.setStatus(RequestStatus.PENDING);
        entity.setPriority(priority);
        entity.setValueDate(valueDate);
        entity.setManualReviewRequired(controlReason != null);
        entity.setControlReason(controlReason);
        entity.setControlPolicyId(policy.policyId());
        entity.setControlPolicySource(policy.sourceBrokerCode());
        entity.setControlLimitPolicyId(risk.policyId());
        entity.setControlLimitPolicySource(formatLimitPolicySource(risk.sourceBrokerCode(), risk.sourceCurrencyCode()));
        entity.setProjectedDailyExposure(risk.projectedExposure());
        entity.setSlaDueAt(calculateSlaDueAt(priority));
        entity.setReason(request.reason().trim());
        entity.setRequestedBy(actor);

        entity = fxRequestRepository.save(entity);
        auditLogService.log(
                actor,
                "CREATE_FX_REQUEST",
                "FX_REQUEST",
                entity.getId().toString(),
                "pair=%s/%s amount=%s priority=%s valueDate=%s approvalPolicy=%s riskPolicy=%s projectedExposure=%s".formatted(
                        entity.getFromCurrency(),
                        entity.getToCurrency(),
                        entity.getAmount(),
                        entity.getPriority(),
                        entity.getValueDate(),
                        entity.getControlPolicyId(),
                        entity.getControlLimitPolicyId(),
                        entity.getProjectedDailyExposure()
                )
        );
        return toResponse(entity);
    }

    @Transactional
    public RequestResponse approve(UUID requestId, String reviewReason, String actor) {
        Optional<CashRequest> cashOpt = cashRequestRepository.findById(requestId);
        if (cashOpt.isPresent()) {
            CashRequest entity = cashOpt.get();
            validateReviewInput(entity.getRequestedBy(), entity.isManualReviewRequired(), reviewReason, actor, requestId);
            return approveCash(entity, reviewReason, actor);
        }

        FxRequest fx = fxRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        validateReviewInput(fx.getRequestedBy(), fx.isManualReviewRequired(), reviewReason, actor, requestId);
        return approveFx(fx, reviewReason, actor);
    }

    @Transactional
    public RequestResponse reject(UUID requestId, String reviewReason, String actor) {
        Optional<CashRequest> cashOpt = cashRequestRepository.findById(requestId);
        if (cashOpt.isPresent()) {
            CashRequest entity = cashOpt.get();
            validateReviewInput(entity.getRequestedBy(), entity.isManualReviewRequired(), reviewReason, actor, requestId);
            ensurePending(entity.getStatus(), requestId);
            entity.setStatus(RequestStatus.REJECTED);
            entity.setReviewedBy(actor);
            entity.setReviewReason(reviewReason.trim());
            entity.setReviewedAt(OffsetDateTime.now());
            cashRequestRepository.save(entity);
            auditLogService.log(actor, "REJECT_CASH_REQUEST", "CASH_REQUEST", entity.getId().toString(), reviewReason.trim());
            return toResponse(entity);
        }

        FxRequest fx = fxRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        validateReviewInput(fx.getRequestedBy(), fx.isManualReviewRequired(), reviewReason, actor, requestId);
        ensurePending(fx.getStatus(), requestId);
        fx.setStatus(RequestStatus.REJECTED);
        fx.setReviewedBy(actor);
        fx.setReviewReason(reviewReason.trim());
        fx.setReviewedAt(OffsetDateTime.now());
        fxRequestRepository.save(fx);
        auditLogService.log(actor, "REJECT_FX_REQUEST", "FX_REQUEST", fx.getId().toString(), reviewReason.trim());
        return toResponse(fx);
    }

    @Transactional(readOnly = true)
    public Page<RequestResponse> listCashRequests(
            RequestStatus status,
            Long accountId,
            String keyword,
            String filter,
            Pageable pageable
    ) {
        return cashRequestRepository.search(status, accountId, keyword, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RequestResponse> listFxRequests(
            RequestStatus status,
            Long accountId,
            String keyword,
            String filter,
            Pageable pageable
    ) {
        return fxRequestRepository.search(status, accountId, keyword, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RequestResponse getCashRequest(UUID requestId) {
        CashRequest request = cashRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Cash request not found: " + requestId));
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public RequestResponse getFxRequest(UUID requestId) {
        FxRequest request = fxRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("FX request not found: " + requestId));
        return toResponse(request);
    }

    private RequestResponse approveCash(CashRequest entity, String reviewReason, String actor) {
        ensurePending(entity.getStatus(), entity.getId());
        int attempts = 0;
        boolean success = false;
        Exception lastError = null;
        while (attempts < 3 && !success) {
            attempts++;
            try {
                brokerAdapter.submitCashInstruction(entity.getId());
                success = true;
            } catch (Exception ex) {
                lastError = ex;
            }
        }

        entity.setReviewedBy(actor);
        entity.setReviewReason(reviewReason.trim());
        entity.setReviewedAt(OffsetDateTime.now());
        entity.setStatus(success ? RequestStatus.APPROVED : RequestStatus.FAILED);
        cashRequestRepository.save(entity);

        if (success) {
            auditLogService.log(actor, "APPROVE_CASH_REQUEST", "CASH_REQUEST", entity.getId().toString(), reviewReason.trim());
        } else {
            auditLogService.log(actor, "FAIL_CASH_REQUEST", "CASH_REQUEST", entity.getId().toString(), safeMessage(lastError));
            opsCaseService.openForRequestFailure(
                    "CASH_REQUEST",
                    entity.getId().toString(),
                    entity.getAccount().getId(),
                    actor,
                    safeMessage(lastError)
            );
        }
        return toResponse(entity);
    }

    private RequestResponse approveFx(FxRequest entity, String reviewReason, String actor) {
        ensurePending(entity.getStatus(), entity.getId());
        int attempts = 0;
        boolean success = false;
        Exception lastError = null;
        while (attempts < 3 && !success) {
            attempts++;
            try {
                brokerAdapter.submitFxInstruction(entity.getId());
                success = true;
            } catch (Exception ex) {
                lastError = ex;
            }
        }

        entity.setReviewedBy(actor);
        entity.setReviewReason(reviewReason.trim());
        entity.setReviewedAt(OffsetDateTime.now());
        entity.setStatus(success ? RequestStatus.APPROVED : RequestStatus.FAILED);
        fxRequestRepository.save(entity);

        if (success) {
            auditLogService.log(actor, "APPROVE_FX_REQUEST", "FX_REQUEST", entity.getId().toString(), reviewReason.trim());
        } else {
            auditLogService.log(actor, "FAIL_FX_REQUEST", "FX_REQUEST", entity.getId().toString(), safeMessage(lastError));
            opsCaseService.openForRequestFailure(
                    "FX_REQUEST",
                    entity.getId().toString(),
                    entity.getAccount().getId(),
                    actor,
                    safeMessage(lastError)
            );
        }
        return toResponse(entity);
    }

    private void ensurePending(RequestStatus status, Object requestId) {
        if (status != RequestStatus.PENDING) {
            throw new BadRequestException("Request is already finalized: " + requestId);
        }
    }

    private void ensureAccountOperable(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not operable for requests: " + account.getStatus());
        }
    }

    private void validateCurrency(String currency) {
        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            throw new BadRequestException("Unsupported currency: " + currency);
        }
    }

    private String normalizeCurrency(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Currency is required");
        }
        return raw.trim().toUpperCase();
    }

    private void validateReason(String reason, String label) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException(label + " is required");
        }
        if (reason.trim().length() < MIN_REASON_LENGTH) {
            throw new BadRequestException(label + " must be at least " + MIN_REASON_LENGTH + " characters");
        }
    }

    private void validateReviewInput(
            String requestedBy,
            boolean manualReviewRequired,
            String reviewReason,
            String actor,
            UUID requestId
    ) {
        validateReason(reviewReason, "Review reason");
        if (reviewReason.trim().length() < MIN_REVIEW_REASON_LENGTH) {
            throw new BadRequestException("Review reason must be at least " + MIN_REVIEW_REASON_LENGTH + " characters");
        }
        if (requestedBy != null && requestedBy.equalsIgnoreCase(actor)) {
            throw new BadRequestException("4-eyes control violation for request: " + requestId);
        }
        if (manualReviewRequired && reviewReason.trim().length() < MIN_MANUAL_REVIEW_REASON_LENGTH) {
            throw new BadRequestException(
                    "Manual-review request requires at least " + MIN_MANUAL_REVIEW_REASON_LENGTH + " chars in review reason"
            );
        }
    }

    private void ensureNoDuplicatePendingCash(
            Long accountId,
            CashRequestType type,
            String currency,
            BigDecimal amount
    ) {
        OffsetDateTime windowStart = OffsetDateTime.now().minus(DUPLICATE_WINDOW);
        boolean duplicated = cashRequestRepository.existsByAccountIdAndTypeAndCurrencyAndAmountAndStatusAndRequestedAtAfter(
                accountId,
                type,
                currency,
                amount,
                RequestStatus.PENDING,
                windowStart
        );
        if (duplicated) {
            throw new BadRequestException("Duplicate pending cash request exists in the last 10 minutes");
        }
    }

    private void ensureNoDuplicatePendingFx(
            Long accountId,
            String fromCurrency,
            String toCurrency,
            BigDecimal amount
    ) {
        OffsetDateTime windowStart = OffsetDateTime.now().minus(DUPLICATE_WINDOW);
        boolean duplicated = fxRequestRepository.existsByAccountIdAndFromCurrencyAndToCurrencyAndAmountAndStatusAndRequestedAtAfter(
                accountId,
                fromCurrency,
                toCurrency,
                amount,
                RequestStatus.PENDING,
                windowStart
        );
        if (duplicated) {
            throw new BadRequestException("Duplicate pending fx request exists in the last 10 minutes");
        }
    }

    private void ensureSufficientBalance(Long accountId, String currency, BigDecimal requestedAmount, String context) {
        Balance latest = balanceRepository.findFirstByAccountIdAndCurrencyOrderByTradingDateDesc(accountId, currency);
        BigDecimal available = latest == null ? BigDecimal.ZERO : latest.getAmount();
        if (requestedAmount.compareTo(available) > 0) {
            throw new BadRequestException(
                    "Insufficient " + currency + " balance for " + context + ": requested=%s available=%s"
                            .formatted(requestedAmount, available)
            );
        }
    }

    private LocalDate resolveValueDate(String broker, LocalDate requestedValueDate) {
        BrokerCutoff cutoff = resolveCutoff(broker);
        OffsetDateTime now = OffsetDateTime.now(cutoff.zoneId());
        LocalDate today = now.toLocalDate();
        LocalDate valueDate = requestedValueDate == null ? today : requestedValueDate;

        if (valueDate.isBefore(today)) {
            throw new BadRequestException("valueDate cannot be in the past");
        }
        if (valueDate.isEqual(today) && now.toLocalTime().isAfter(cutoff.cutoffTime())) {
            throw new BadRequestException(
                    "Same-day instruction cutoff has passed for broker " + broker + ". Use next business date."
            );
        }
        return valueDate;
    }

    private BrokerCutoff resolveCutoff(String broker) {
        if (broker == null || broker.isBlank()) {
            return DEFAULT_CUTOFF;
        }
        return BROKER_CUTOFFS.getOrDefault(broker.trim().toUpperCase(), DEFAULT_CUTOFF);
    }

    private RequestPriority resolvePriority(
            RequestPriority requested,
            BigDecimal amount,
            boolean manualReviewRequired,
            BigDecimal highThreshold,
            BigDecimal urgentThreshold
    ) {
        RequestPriority minimum;
        if (amount.compareTo(urgentThreshold) >= 0) {
            minimum = RequestPriority.URGENT;
        } else if (amount.compareTo(highThreshold) >= 0) {
            minimum = RequestPriority.HIGH;
        } else {
            minimum = RequestPriority.NORMAL;
        }

        if (manualReviewRequired && minimum.ordinal() < RequestPriority.HIGH.ordinal()) {
            minimum = RequestPriority.HIGH;
        }

        if (requested == null) {
            return minimum;
        }
        return requested.ordinal() > minimum.ordinal() ? requested : minimum;
    }

    private OffsetDateTime calculateSlaDueAt(RequestPriority priority) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (priority) {
            case LOW -> now.plusHours(8);
            case NORMAL -> now.plusHours(4);
            case HIGH -> now.plusHours(2);
            case URGENT -> now.plusMinutes(30);
        };
    }

    private String buildCashControlReason(
            CashRequestType type,
            BigDecimal amount,
            LocalDate valueDate,
            ApprovalPolicyService.EffectiveApprovalPolicy policy
    ) {
        List<String> reasons = new ArrayList<>();
        if (amount.compareTo(policy.manualReviewThreshold()) >= 0) {
            reasons.add(type == CashRequestType.WITHDRAW
                    ? "Withdrawal exceeds manual-review threshold"
                    : "Deposit exceeds manual-review threshold");
        }
        if (amount.compareTo(policy.urgentThreshold()) >= 0) {
            reasons.add("Exceptional notional size");
        }
        if (policy.sameDayAutoReview() && valueDate.equals(LocalDate.now())) {
            reasons.add("Same-day settlement");
        }
        return reasons.isEmpty() ? null : String.join("; ", reasons);
    }

    private String buildFxControlReason(
            BigDecimal amount,
            String fromCurrency,
            String toCurrency,
            LocalDate valueDate,
            ApprovalPolicyService.EffectiveApprovalPolicy policy
    ) {
        List<String> reasons = new ArrayList<>();
        if (amount.compareTo(policy.manualReviewThreshold()) >= 0) {
            reasons.add("Large FX conversion amount");
        }
        if (("KRW".equals(fromCurrency) || "KRW".equals(toCurrency)) && amount.compareTo(policy.highThreshold()) >= 0) {
            reasons.add("High-value KRW leg");
        }
        if (policy.sameDayAutoReview() && valueDate.equals(LocalDate.now())) {
            reasons.add("Same-day value date");
        }
        return reasons.isEmpty() ? null : String.join("; ", reasons);
    }

    private String buildRiskControlReason(RiskLimitPolicyService.RiskLimitEvaluation riskEvaluation) {
        if (!riskEvaluation.softLimitBreached()) {
            return null;
        }
        return "Projected daily exposure exceeds soft limit (%s > %s)"
                .formatted(riskEvaluation.projectedExposure(), riskEvaluation.dailySoftLimit());
    }

    private String mergeControlReasons(String... reasons) {
        List<String> normalized = new ArrayList<>();
        for (String reason : reasons) {
            if (reason != null && !reason.isBlank()) {
                normalized.add(reason.trim());
            }
        }
        return normalized.isEmpty() ? null : String.join("; ", normalized);
    }

    private String formatLimitPolicySource(String brokerCode, String currencyCode) {
        String broker = (brokerCode == null || brokerCode.isBlank()) ? "DEFAULT" : brokerCode;
        String currency = (currencyCode == null || currencyCode.isBlank()) ? "*" : currencyCode;
        return broker + "/" + currency;
    }

    private String safeMessage(Exception error) {
        if (error == null || error.getMessage() == null) {
            return "n/a";
        }
        return error.getMessage();
    }

    private RequestResponse toResponse(CashRequest entity) {
        return new RequestResponse(
                entity.getId(),
                "CASH_" + entity.getType().name(),
                entity.getStatus(),
                entity.getAccount().getId(),
                entity.getAccount().getAccountNo(),
                entity.getAmount(),
                entity.getCurrency(),
                null,
                null,
                null,
                null,
                null,
                null,
                entity.getPriority(),
                entity.getValueDate(),
                entity.isManualReviewRequired(),
                entity.getControlReason(),
                entity.getControlPolicyId(),
                entity.getControlPolicySource(),
                entity.getControlLimitPolicyId(),
                entity.getControlLimitPolicySource(),
                entity.getProjectedDailyExposure(),
                entity.getSlaDueAt(),
                entity.getRequestedBy(),
                entity.getReviewedBy(),
                entity.getReason(),
                entity.getReviewReason(),
                entity.getRequestedAt(),
                entity.getReviewedAt()
        );
    }

    private RequestResponse toResponse(FxRequest entity) {
        return new RequestResponse(
                entity.getId(),
                "FX_" + entity.getFromCurrency() + "_" + entity.getToCurrency(),
                entity.getStatus(),
                entity.getAccount().getId(),
                entity.getAccount().getAccountNo(),
                entity.getAmount(),
                entity.getFromCurrency() + "/" + entity.getToCurrency(),
                entity.getFromCurrency(),
                entity.getToCurrency(),
                entity.getExchangeRate(),
                entity.getExpectedToAmount(),
                entity.getExchangeRateDate(),
                entity.getExchangeRateSource(),
                entity.getPriority(),
                entity.getValueDate(),
                entity.isManualReviewRequired(),
                entity.getControlReason(),
                entity.getControlPolicyId(),
                entity.getControlPolicySource(),
                entity.getControlLimitPolicyId(),
                entity.getControlLimitPolicySource(),
                entity.getProjectedDailyExposure(),
                entity.getSlaDueAt(),
                entity.getRequestedBy(),
                entity.getReviewedBy(),
                entity.getReason(),
                entity.getReviewReason(),
                entity.getRequestedAt(),
                entity.getReviewedAt()
        );
    }

    private record BrokerCutoff(ZoneId zoneId, LocalTime cutoffTime) {
    }
}
