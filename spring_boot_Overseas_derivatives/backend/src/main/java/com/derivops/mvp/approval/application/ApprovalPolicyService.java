package com.derivops.mvp.approval.application;
import com.derivops.mvp.approval.*;
import com.derivops.mvp.approval.api.*;
import com.derivops.mvp.approval.dto.*;
import com.derivops.mvp.approval.infrastructure.*;


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
public class ApprovalPolicyService {

    private static final BigDecimal DEFAULT_HIGH_THRESHOLD = new BigDecimal("250000");
    private static final BigDecimal DEFAULT_URGENT_THRESHOLD = new BigDecimal("1000000");

    private final ApprovalPolicyRepository approvalPolicyRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<ApprovalPolicyResponse> list(String keyword, String filter, Pageable pageable) {
        return approvalPolicyRepository.search(keyword, filter, pageable).map(this::toResponse);
    }

    @Transactional
    public ApprovalPolicyResponse create(UpsertApprovalPolicyRequest request, String actor) {
        ApprovalPolicy entity = new ApprovalPolicy();
        apply(entity, request);
        entity = approvalPolicyRepository.save(entity);
        auditLogService.log(actor, "CREATE_APPROVAL_POLICY", "APPROVAL_POLICY", String.valueOf(entity.getId()), detail(entity));
        return toResponse(entity);
    }

    @Transactional
    public ApprovalPolicyResponse update(Long id, UpsertApprovalPolicyRequest request, String actor) {
        ApprovalPolicy entity = approvalPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Approval policy not found: " + id));
        apply(entity, request);
        entity = approvalPolicyRepository.save(entity);
        auditLogService.log(actor, "UPDATE_APPROVAL_POLICY", "APPROVAL_POLICY", String.valueOf(entity.getId()), detail(entity));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public EffectiveApprovalPolicy resolve(String brokerCode, ApprovalDomain domain, LocalDate targetDate) {
        LocalDate date = targetDate == null ? LocalDate.now() : targetDate;
        String normalizedBroker = normalizeBrokerCode(brokerCode);

        return approvalPolicyRepository.resolve(normalizedBroker, domain, date)
                .or(() -> approvalPolicyRepository.resolve("GLOBAL", domain, date))
                .map(policy -> new EffectiveApprovalPolicy(
                        policy.getHighThreshold(),
                        policy.getUrgentThreshold(),
                        policy.getManualReviewThreshold(),
                        policy.isSameDayAutoReview(),
                        policy.getId(),
                        policy.getBrokerCode()
                ))
                .orElseGet(() -> new EffectiveApprovalPolicy(
                        DEFAULT_HIGH_THRESHOLD,
                        DEFAULT_URGENT_THRESHOLD,
                        DEFAULT_HIGH_THRESHOLD,
                        true,
                        null,
                        "DEFAULT"
                ));
    }

    private void apply(ApprovalPolicy entity, UpsertApprovalPolicyRequest request) {
        if (request.highThreshold().compareTo(request.urgentThreshold()) > 0) {
            throw new BadRequestException("highThreshold must be <= urgentThreshold");
        }
        if (request.manualReviewThreshold().compareTo(request.urgentThreshold()) > 0) {
            throw new BadRequestException("manualReviewThreshold must be <= urgentThreshold");
        }
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BadRequestException("effectiveTo must be >= effectiveFrom");
        }

        entity.setBrokerCode(normalizeBrokerCode(request.brokerCode()));
        entity.setDomain(request.domain());
        entity.setHighThreshold(request.highThreshold());
        entity.setUrgentThreshold(request.urgentThreshold());
        entity.setManualReviewThreshold(request.manualReviewThreshold());
        entity.setSameDayAutoReview(request.sameDayAutoReview());
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

    private String detail(ApprovalPolicy policy) {
        return "broker=%s domain=%s high=%s urgent=%s manual=%s sameDay=%s enabled=%s".formatted(
                policy.getBrokerCode(),
                policy.getDomain(),
                policy.getHighThreshold(),
                policy.getUrgentThreshold(),
                policy.getManualReviewThreshold(),
                policy.isSameDayAutoReview(),
                policy.isEnabled()
        );
    }

    private ApprovalPolicyResponse toResponse(ApprovalPolicy item) {
        return new ApprovalPolicyResponse(
                item.getId(),
                item.getBrokerCode(),
                item.getDomain(),
                item.getHighThreshold(),
                item.getUrgentThreshold(),
                item.getManualReviewThreshold(),
                item.isSameDayAutoReview(),
                item.isEnabled(),
                item.getEffectiveFrom(),
                item.getEffectiveTo(),
                item.getDescription(),
                item.getCreatedAt()
        );
    }

    public record EffectiveApprovalPolicy(
            BigDecimal highThreshold,
            BigDecimal urgentThreshold,
            BigDecimal manualReviewThreshold,
            boolean sameDayAutoReview,
            Long policyId,
            String sourceBrokerCode
    ) {
    }
}
