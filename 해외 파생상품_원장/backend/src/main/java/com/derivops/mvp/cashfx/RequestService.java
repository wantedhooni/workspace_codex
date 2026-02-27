package com.derivops.mvp.cashfx;

import com.derivops.mvp.account.Account;
import com.derivops.mvp.account.AccountRepository;
import com.derivops.mvp.audit.AuditLogService;
import com.derivops.mvp.common.BadRequestException;
import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.common.rsql.RsqlSpecificationBuilder;
import com.derivops.mvp.integration.BrokerAdapter;
import jakarta.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RequestService {
    private static final Map<String, String> CASH_FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("status", "status"),
            Map.entry("accountId", "account.id"),
            Map.entry("accountNo", "account.accountNo"),
            Map.entry("type", "type"),
            Map.entry("currency", "currency"),
            Map.entry("amount", "amount"),
            Map.entry("requestedBy", "requestedBy"),
            Map.entry("reviewedBy", "reviewedBy"),
            Map.entry("requestedAt", "requestedAt"),
            Map.entry("reviewedAt", "reviewedAt"),
            Map.entry("reason", "reason"),
            Map.entry("reviewReason", "reviewReason")
    );

    private static final Map<String, String> FX_FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("status", "status"),
            Map.entry("accountId", "account.id"),
            Map.entry("accountNo", "account.accountNo"),
            Map.entry("fromCurrency", "fromCurrency"),
            Map.entry("toCurrency", "toCurrency"),
            Map.entry("amount", "amount"),
            Map.entry("requestedBy", "requestedBy"),
            Map.entry("reviewedBy", "reviewedBy"),
            Map.entry("requestedAt", "requestedAt"),
            Map.entry("reviewedAt", "reviewedAt"),
            Map.entry("reason", "reason"),
            Map.entry("reviewReason", "reviewReason")
    );

    private final CashRequestRepository cashRequestRepository;
    private final FxRequestRepository fxRequestRepository;
    private final AccountRepository accountRepository;
    private final BrokerAdapter brokerAdapter;
    private final AuditLogService auditLogService;

    @Transactional
    public RequestResponse createCashRequest(CreateCashRequest request, String actor) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found: " + request.accountId()));

        CashRequest entity = new CashRequest();
        entity.setAccount(account);
        entity.setType(request.type());
        entity.setAmount(request.amount());
        entity.setCurrency(request.currency().toUpperCase());
        entity.setStatus(RequestStatus.PENDING);
        entity.setReason(request.reason());
        entity.setRequestedBy(actor);

        entity = cashRequestRepository.save(entity);
        auditLogService.log(actor, "CREATE_CASH_REQUEST", "CASH_REQUEST", entity.getId().toString(), request.reason());
        return toResponse(entity);
    }

    @Transactional
    public RequestResponse createFxRequest(CreateFxRequest request, String actor) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found: " + request.accountId()));

        FxRequest entity = new FxRequest();
        entity.setAccount(account);
        entity.setFromCurrency(request.fromCurrency().toUpperCase());
        entity.setToCurrency(request.toCurrency().toUpperCase());
        entity.setAmount(request.amount());
        entity.setStatus(RequestStatus.PENDING);
        entity.setReason(request.reason());
        entity.setRequestedBy(actor);

        entity = fxRequestRepository.save(entity);
        auditLogService.log(actor, "CREATE_FX_REQUEST", "FX_REQUEST", entity.getId().toString(), request.reason());
        return toResponse(entity);
    }

    @Transactional
    public RequestResponse approve(UUID requestId, String reviewReason, String actor) {
        Optional<CashRequest> cashOpt = cashRequestRepository.findById(requestId);
        if (cashOpt.isPresent()) {
            return approveCash(cashOpt.get(), reviewReason, actor);
        }

        FxRequest fx = fxRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        return approveFx(fx, reviewReason, actor);
    }

    @Transactional
    public RequestResponse reject(UUID requestId, String reviewReason, String actor) {
        Optional<CashRequest> cashOpt = cashRequestRepository.findById(requestId);
        if (cashOpt.isPresent()) {
            CashRequest entity = cashOpt.get();
            ensurePending(entity.getStatus(), requestId);
            entity.setStatus(RequestStatus.REJECTED);
            entity.setReviewedBy(actor);
            entity.setReviewReason(reviewReason);
            entity.setReviewedAt(OffsetDateTime.now());
            cashRequestRepository.save(entity);
            auditLogService.log(actor, "REJECT_CASH_REQUEST", "CASH_REQUEST", entity.getId().toString(), reviewReason);
            return toResponse(entity);
        }

        FxRequest fx = fxRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        ensurePending(fx.getStatus(), requestId);
        fx.setStatus(RequestStatus.REJECTED);
        fx.setReviewedBy(actor);
        fx.setReviewReason(reviewReason);
        fx.setReviewedAt(OffsetDateTime.now());
        fxRequestRepository.save(fx);
        auditLogService.log(actor, "REJECT_FX_REQUEST", "FX_REQUEST", fx.getId().toString(), reviewReason);
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
        Specification<CashRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (accountId != null) {
                predicates.add(cb.equal(root.get("account").get("id"), accountId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String q = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("requestedBy")), q),
                        cb.like(cb.lower(root.get("reason")), q),
                        cb.like(cb.lower(root.get("reviewReason")), q),
                        cb.like(cb.lower(root.get("currency")), q),
                        cb.like(cb.lower(root.get("account").get("accountNo")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        if (filter != null && !filter.isBlank()) {
            spec = spec.and(RsqlSpecificationBuilder.build(filter, CASH_FILTER_FIELDS));
        }
        return cashRequestRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RequestResponse> listFxRequests(
            RequestStatus status,
            Long accountId,
            String keyword,
            String filter,
            Pageable pageable
    ) {
        Specification<FxRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (accountId != null) {
                predicates.add(cb.equal(root.get("account").get("id"), accountId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String q = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("requestedBy")), q),
                        cb.like(cb.lower(root.get("reason")), q),
                        cb.like(cb.lower(root.get("reviewReason")), q),
                        cb.like(cb.lower(root.get("fromCurrency")), q),
                        cb.like(cb.lower(root.get("toCurrency")), q),
                        cb.like(cb.lower(root.get("account").get("accountNo")), q)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        if (filter != null && !filter.isBlank()) {
            spec = spec.and(RsqlSpecificationBuilder.build(filter, FX_FILTER_FIELDS));
        }
        return fxRequestRepository.findAll(spec, pageable).map(this::toResponse);
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
        entity.setReviewReason(reviewReason);
        entity.setReviewedAt(OffsetDateTime.now());
        entity.setStatus(success ? RequestStatus.APPROVED : RequestStatus.FAILED);
        cashRequestRepository.save(entity);

        if (success) {
            auditLogService.log(actor, "APPROVE_CASH_REQUEST", "CASH_REQUEST", entity.getId().toString(), reviewReason);
        } else {
            auditLogService.log(actor, "FAIL_CASH_REQUEST", "CASH_REQUEST", entity.getId().toString(), safeMessage(lastError));
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
        entity.setReviewReason(reviewReason);
        entity.setReviewedAt(OffsetDateTime.now());
        entity.setStatus(success ? RequestStatus.APPROVED : RequestStatus.FAILED);
        fxRequestRepository.save(entity);

        if (success) {
            auditLogService.log(actor, "APPROVE_FX_REQUEST", "FX_REQUEST", entity.getId().toString(), reviewReason);
        } else {
            auditLogService.log(actor, "FAIL_FX_REQUEST", "FX_REQUEST", entity.getId().toString(), safeMessage(lastError));
        }
        return toResponse(entity);
    }

    private void ensurePending(RequestStatus status, Object requestId) {
        if (status != RequestStatus.PENDING) {
            throw new BadRequestException("Request is already finalized: " + requestId);
        }
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
                entity.getAmount(),
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
                entity.getAmount(),
                entity.getRequestedBy(),
                entity.getReviewedBy(),
                entity.getReason(),
                entity.getReviewReason(),
                entity.getRequestedAt(),
                entity.getReviewedAt()
        );
    }
}
