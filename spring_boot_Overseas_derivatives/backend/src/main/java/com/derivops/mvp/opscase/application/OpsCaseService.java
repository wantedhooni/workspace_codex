package com.derivops.mvp.opscase.application;
import com.derivops.mvp.opscase.*;
import com.derivops.mvp.opscase.api.*;
import com.derivops.mvp.opscase.dto.*;
import com.derivops.mvp.opscase.infrastructure.*;


import com.derivops.mvp.audit.application.AuditLogService;
import com.derivops.mvp.common.BadRequestException;
import com.derivops.mvp.common.NotFoundException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OpsCaseService {

    private static final int MIN_COMMENT_LENGTH = 8;
    private static final int MIN_RESOLUTION_LENGTH = 10;

    private final OpsCaseRepository opsCaseRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<OpsCaseResponse> list(
            OpsCaseStatus status,
            OpsCaseSeverity severity,
            String assignee,
            String keyword,
            String filter,
            Pageable pageable
    ) {
        return opsCaseRepository.search(status, severity, assignee, keyword, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OpsCaseResponse get(Long id) {
        OpsCase item = opsCaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Ops case not found: " + id));
        return toResponse(item);
    }

    @Transactional
    public OpsCaseResponse create(CreateOpsCaseRequest request, String actor) {
        OpsCase item = new OpsCase();
        item.setCaseNo(nextCaseNo());
        item.setCategory(request.category());
        item.setSeverity(request.severity());
        item.setStatus(OpsCaseStatus.OPEN);
        item.setTitle(normalizeText(request.title(), 5, "title"));
        item.setDescription(normalizeText(request.description(), 8, "description"));
        item.setAssignee(normalizeOptional(request.assignee()));
        item.setDueAt(request.dueAt());
        item.setLinkedType(normalizeOptional(request.linkedType()));
        item.setLinkedId(normalizeOptional(request.linkedId()));
        item.setAccountId(request.accountId());
        item.setCreatedBy(actor);
        item.setUpdatedBy(actor);

        item = opsCaseRepository.save(item);
        auditLogService.log(actor, "CREATE_OPS_CASE", "OPS_CASE", String.valueOf(item.getId()), detail(item));
        return toResponse(item);
    }

    @Transactional
    public OpsCaseResponse update(Long id, UpdateOpsCaseRequest request, String actor) {
        OpsCase item = opsCaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Ops case not found: " + id));
        if (item.getStatus() == OpsCaseStatus.CLOSED) {
            throw new BadRequestException("Closed case cannot be edited");
        }
        item.setCategory(request.category());
        item.setSeverity(request.severity());
        item.setTitle(normalizeText(request.title(), 5, "title"));
        item.setDescription(normalizeText(request.description(), 8, "description"));
        item.setAssignee(normalizeOptional(request.assignee()));
        item.setDueAt(request.dueAt());
        item.setUpdatedBy(actor);

        item = opsCaseRepository.save(item);
        auditLogService.log(actor, "UPDATE_OPS_CASE", "OPS_CASE", String.valueOf(item.getId()), detail(item));
        return toResponse(item);
    }

    @Transactional
    public OpsCaseResponse start(Long id, TransitionOpsCaseRequest request, String actor) {
        OpsCase item = opsCaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Ops case not found: " + id));
        ensureComment(request.comment(), MIN_COMMENT_LENGTH);
        if (item.getStatus() != OpsCaseStatus.OPEN) {
            throw new BadRequestException("Only OPEN case can be started");
        }
        item.setStatus(OpsCaseStatus.IN_PROGRESS);
        item.setUpdatedBy(actor);
        item = opsCaseRepository.save(item);
        auditLogService.log(actor, "START_OPS_CASE", "OPS_CASE", String.valueOf(item.getId()), request.comment().trim());
        return toResponse(item);
    }

    @Transactional
    public OpsCaseResponse assign(Long id, AssignOpsCaseRequest request, String actor) {
        OpsCase item = opsCaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Ops case not found: " + id));
        if (item.getStatus() == OpsCaseStatus.CLOSED) {
            throw new BadRequestException("Closed case cannot be assigned");
        }
        ensureComment(request.comment(), MIN_COMMENT_LENGTH);
        item.setAssignee(normalizeText(request.assignee(), 3, "assignee"));
        item.setUpdatedBy(actor);
        item = opsCaseRepository.save(item);
        auditLogService.log(actor, "ASSIGN_OPS_CASE", "OPS_CASE", String.valueOf(item.getId()), request.comment().trim());
        return toResponse(item);
    }

    @Transactional
    public OpsCaseResponse resolve(Long id, TransitionOpsCaseRequest request, String actor) {
        OpsCase item = opsCaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Ops case not found: " + id));
        ensureComment(request.comment(), MIN_RESOLUTION_LENGTH);
        if (item.getStatus() == OpsCaseStatus.CLOSED) {
            throw new BadRequestException("Closed case cannot be resolved");
        }
        item.setStatus(OpsCaseStatus.RESOLVED);
        item.setResolutionSummary(request.comment().trim());
        item.setResolvedBy(actor);
        item.setResolvedAt(OffsetDateTime.now());
        item.setUpdatedBy(actor);
        item = opsCaseRepository.save(item);
        auditLogService.log(actor, "RESOLVE_OPS_CASE", "OPS_CASE", String.valueOf(item.getId()), request.comment().trim());
        return toResponse(item);
    }

    @Transactional
    public OpsCaseResponse close(Long id, TransitionOpsCaseRequest request, String actor) {
        OpsCase item = opsCaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Ops case not found: " + id));
        ensureComment(request.comment(), MIN_COMMENT_LENGTH);
        if (item.getStatus() != OpsCaseStatus.RESOLVED) {
            throw new BadRequestException("Only RESOLVED case can be closed");
        }
        item.setStatus(OpsCaseStatus.CLOSED);
        item.setClosedBy(actor);
        item.setClosedAt(OffsetDateTime.now());
        item.setUpdatedBy(actor);
        item = opsCaseRepository.save(item);
        auditLogService.log(actor, "CLOSE_OPS_CASE", "OPS_CASE", String.valueOf(item.getId()), request.comment().trim());
        return toResponse(item);
    }

    @Transactional
    public OpsCaseResponse reopen(Long id, TransitionOpsCaseRequest request, String actor) {
        OpsCase item = opsCaseRepository.findById(id).orElseThrow(() -> new NotFoundException("Ops case not found: " + id));
        ensureComment(request.comment(), MIN_COMMENT_LENGTH);
        if (item.getStatus() != OpsCaseStatus.RESOLVED && item.getStatus() != OpsCaseStatus.CLOSED) {
            throw new BadRequestException("Only RESOLVED/CLOSED case can be reopened");
        }
        item.setStatus(OpsCaseStatus.OPEN);
        item.setClosedBy(null);
        item.setClosedAt(null);
        item.setUpdatedBy(actor);
        item = opsCaseRepository.save(item);
        auditLogService.log(actor, "REOPEN_OPS_CASE", "OPS_CASE", String.valueOf(item.getId()), request.comment().trim());
        return toResponse(item);
    }

    @Transactional
    public void openForRequestFailure(
            String linkedType,
            String linkedId,
            Long accountId,
            String actor,
            String failureMessage
    ) {
        if (linkedType == null || linkedId == null) {
            return;
        }
        boolean existsOpen = opsCaseRepository.findFirstByLinkedTypeAndLinkedIdAndStatusInOrderByCreatedAtDesc(
                linkedType,
                linkedId,
                List.of(OpsCaseStatus.OPEN, OpsCaseStatus.IN_PROGRESS)
        ).isPresent();
        if (existsOpen) {
            return;
        }

        OpsCase item = new OpsCase();
        item.setCaseNo(nextCaseNo());
        item.setCategory(OpsCaseCategory.REQUEST_FAILURE);
        item.setSeverity(OpsCaseSeverity.HIGH);
        item.setStatus(OpsCaseStatus.OPEN);
        item.setTitle("Broker submission failed: " + linkedType);
        item.setDescription("linkedId=%s, failure=%s".formatted(linkedId, safe(failureMessage)));
        item.setLinkedType(linkedType);
        item.setLinkedId(linkedId);
        item.setAccountId(accountId);
        item.setDueAt(OffsetDateTime.now().plusHours(2));
        item.setCreatedBy(actor);
        item.setUpdatedBy(actor);
        opsCaseRepository.save(item);
        auditLogService.log(actor, "CREATE_OPS_CASE_AUTO", "OPS_CASE", item.getCaseNo(), detail(item));
    }

    private String nextCaseNo() {
        String ts = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        return "OPS-" + ts + "-" + suffix;
    }

    private String safe(String message) {
        if (message == null || message.isBlank()) {
            return "n/a";
        }
        return message.trim();
    }

    private void ensureComment(String comment, int minLength) {
        if (comment == null || comment.isBlank() || comment.trim().length() < minLength) {
            throw new BadRequestException("Comment must be at least " + minLength + " characters");
        }
    }

    private String normalizeText(String value, int minLength, String label) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(label + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() < minLength) {
            throw new BadRequestException(label + " must be at least " + minLength + " characters");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String detail(OpsCase item) {
        return "caseNo=%s status=%s severity=%s linked=%s:%s assignee=%s".formatted(
                item.getCaseNo(),
                item.getStatus(),
                item.getSeverity(),
                item.getLinkedType(),
                item.getLinkedId(),
                item.getAssignee()
        );
    }

    private OpsCaseResponse toResponse(OpsCase item) {
        return new OpsCaseResponse(
                item.getId(),
                item.getCaseNo(),
                item.getCategory(),
                item.getSeverity(),
                item.getStatus(),
                item.getTitle(),
                item.getDescription(),
                item.getAssignee(),
                item.getDueAt(),
                item.getLinkedType(),
                item.getLinkedId(),
                item.getAccountId(),
                item.getResolutionSummary(),
                item.getCreatedBy(),
                item.getUpdatedBy(),
                item.getResolvedBy(),
                item.getResolvedAt(),
                item.getClosedBy(),
                item.getClosedAt(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
