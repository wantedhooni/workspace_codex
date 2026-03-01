package com.derivops.mvp.risk.application;
import com.derivops.mvp.risk.*;
import com.derivops.mvp.risk.api.*;
import com.derivops.mvp.risk.dto.*;
import com.derivops.mvp.risk.infrastructure.*;


import com.derivops.mvp.approval.ApprovalDomain;
import com.derivops.mvp.audit.application.AuditLogService;
import com.derivops.mvp.common.BadRequestException;
import com.derivops.mvp.common.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RiskLimitPolicyService {

    private static final BigDecimal DEFAULT_MAX_PER_REQUEST = new BigDecimal("1500000");
    private static final BigDecimal DEFAULT_DAILY_SOFT_LIMIT = new BigDecimal("3000000");
    private static final BigDecimal DEFAULT_DAILY_HARD_LIMIT = new BigDecimal("5000000");

    private final RiskLimitPolicyRepository riskLimitPolicyRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<RiskLimitPolicyResponse> list(String keyword, String filter, Pageable pageable) {
        return riskLimitPolicyRepository.search(keyword, filter, pageable).map(this::toResponse);
    }

    @Transactional
    public RiskLimitPolicyResponse create(UpsertRiskLimitPolicyRequest request, String actor) {
        RiskLimitPolicy entity = new RiskLimitPolicy();
        apply(entity, request);
        entity = riskLimitPolicyRepository.save(entity);
        auditLogService.log(actor, "CREATE_RISK_LIMIT_POLICY", "RISK_LIMIT_POLICY", String.valueOf(entity.getId()), detail(entity));
        return toResponse(entity);
    }

    @Transactional
    public RiskLimitPolicyResponse update(Long id, UpsertRiskLimitPolicyRequest request, String actor) {
        RiskLimitPolicy entity = riskLimitPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Risk limit policy not found: " + id));
        apply(entity, request);
        entity = riskLimitPolicyRepository.save(entity);
        auditLogService.log(actor, "UPDATE_RISK_LIMIT_POLICY", "RISK_LIMIT_POLICY", String.valueOf(entity.getId()), detail(entity));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public EffectiveRiskLimitPolicy resolve(String brokerCode, ApprovalDomain domain, String currencyCode, LocalDate targetDate) {
        LocalDate date = targetDate == null ? LocalDate.now() : targetDate;
        String normalizedBroker = normalizeBrokerCode(brokerCode);
        String normalizedCurrency = normalizeCurrencyCode(currencyCode);

        return riskLimitPolicyRepository.resolve(normalizedBroker, domain, normalizedCurrency, date)
                .or(() -> riskLimitPolicyRepository.resolve("GLOBAL", domain, normalizedCurrency, date))
                .map(policy -> new EffectiveRiskLimitPolicy(
                        policy.getMaxPerRequest(),
                        policy.getDailySoftLimit(),
                        policy.getDailyHardLimit(),
                        policy.getId(),
                        policy.getBrokerCode(),
                        policy.getCurrencyCode()
                ))
                .orElseGet(() -> new EffectiveRiskLimitPolicy(
                        DEFAULT_MAX_PER_REQUEST,
                        DEFAULT_DAILY_SOFT_LIMIT,
                        DEFAULT_DAILY_HARD_LIMIT,
                        null,
                        "DEFAULT",
                        normalizedCurrency
                ));
    }

    @Transactional(readOnly = true)
    public RiskLimitEvaluation evaluate(
            String brokerCode,
            ApprovalDomain domain,
            String currencyCode,
            LocalDate targetDate,
            BigDecimal requestAmount,
            BigDecimal currentExposure
    ) {
        EffectiveRiskLimitPolicy policy = resolve(brokerCode, domain, currencyCode, targetDate);
        BigDecimal amount = requestAmount == null ? BigDecimal.ZERO : requestAmount;
        BigDecimal exposureBefore = currentExposure == null ? BigDecimal.ZERO : currentExposure;

        if (amount.compareTo(policy.maxPerRequest()) > 0) {
            throw new BadRequestException(
                    "Amount exceeds per-request risk limit: amount=%s maxPerRequest=%s".formatted(amount, policy.maxPerRequest())
            );
        }

        BigDecimal projected = exposureBefore.add(amount);
        if (projected.compareTo(policy.dailyHardLimit()) > 0) {
            throw new BadRequestException(
                    "Projected daily exposure exceeds hard limit: projected=%s hardLimit=%s".formatted(projected, policy.dailyHardLimit())
            );
        }

        boolean softBreached = projected.compareTo(policy.dailySoftLimit()) > 0;
        return new RiskLimitEvaluation(
                policy.policyId(),
                policy.sourceBrokerCode(),
                policy.sourceCurrencyCode(),
                policy.maxPerRequest(),
                policy.dailySoftLimit(),
                policy.dailyHardLimit(),
                exposureBefore,
                projected,
                softBreached
        );
    }

    private void apply(RiskLimitPolicy entity, UpsertRiskLimitPolicyRequest request) {
        if (request.maxPerRequest().compareTo(request.dailySoftLimit()) > 0) {
            throw new BadRequestException("maxPerRequest must be <= dailySoftLimit");
        }
        if (request.dailySoftLimit().compareTo(request.dailyHardLimit()) > 0) {
            throw new BadRequestException("dailySoftLimit must be <= dailyHardLimit");
        }
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BadRequestException("effectiveTo must be >= effectiveFrom");
        }

        entity.setBrokerCode(normalizeBrokerCode(request.brokerCode()));
        entity.setDomain(request.domain());
        entity.setCurrencyCode(normalizeCurrencyCode(request.currencyCode()));
        entity.setMaxPerRequest(request.maxPerRequest());
        entity.setDailySoftLimit(request.dailySoftLimit());
        entity.setDailyHardLimit(request.dailyHardLimit());
        entity.setEnabled(request.enabled());
        entity.setEffectiveFrom(request.effectiveFrom());
        entity.setEffectiveTo(request.effectiveTo());
        entity.setDescription(request.description());
    }

    private String normalizeBrokerCode(String brokerCode) {
        if (brokerCode == null || brokerCode.isBlank()) {
            return "GLOBAL";
        }
        return brokerCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCurrencyCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }

    private String detail(RiskLimitPolicy policy) {
        return "broker=%s domain=%s ccy=%s max=%s soft=%s hard=%s enabled=%s".formatted(
                policy.getBrokerCode(),
                policy.getDomain(),
                policy.getCurrencyCode(),
                policy.getMaxPerRequest(),
                policy.getDailySoftLimit(),
                policy.getDailyHardLimit(),
                policy.isEnabled()
        );
    }

    private RiskLimitPolicyResponse toResponse(RiskLimitPolicy item) {
        return new RiskLimitPolicyResponse(
                item.getId(),
                item.getBrokerCode(),
                item.getDomain(),
                item.getCurrencyCode(),
                item.getMaxPerRequest(),
                item.getDailySoftLimit(),
                item.getDailyHardLimit(),
                item.isEnabled(),
                item.getEffectiveFrom(),
                item.getEffectiveTo(),
                item.getDescription(),
                item.getCreatedAt()
        );
    }

    public record EffectiveRiskLimitPolicy(
            BigDecimal maxPerRequest,
            BigDecimal dailySoftLimit,
            BigDecimal dailyHardLimit,
            Long policyId,
            String sourceBrokerCode,
            String sourceCurrencyCode
    ) {
    }

    public record RiskLimitEvaluation(
            Long policyId,
            String sourceBrokerCode,
            String sourceCurrencyCode,
            BigDecimal maxPerRequest,
            BigDecimal dailySoftLimit,
            BigDecimal dailyHardLimit,
            BigDecimal exposureBefore,
            BigDecimal projectedExposure,
            boolean softLimitBreached
    ) {
    }
}
