package com.derivops.mvp.audit;

import jakarta.persistence.criteria.Predicate;
import com.derivops.mvp.common.rsql.RsqlSpecificationBuilder;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuditLogService {
    private static final Map<String, String> AUDIT_FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("actor", "actor"),
            Map.entry("action", "action"),
            Map.entry("targetType", "targetType"),
            Map.entry("targetId", "targetId"),
            Map.entry("details", "details"),
            Map.entry("createdAt", "createdAt")
    );

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String actor, String action, String targetType, String targetId, String details) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(
            String actor,
            String action,
            String keyword,
            String filter,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (actor != null && !actor.isBlank()) {
                predicates.add(cb.equal(root.get("actor"), actor));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (keyword != null && !keyword.isBlank()) {
                String q = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("actor")), q),
                        cb.like(cb.lower(root.get("action")), q),
                        cb.like(cb.lower(root.get("targetType")), q),
                        cb.like(cb.lower(root.get("targetId")), q),
                        cb.like(cb.lower(root.get("details")), q)
                ));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        if (filter != null && !filter.isBlank()) {
            spec = spec.and(RsqlSpecificationBuilder.build(filter, AUDIT_FILTER_FIELDS));
        }

        return auditLogRepository.findAll(spec, pageable)
                .map(a -> new AuditLogResponse(
                        a.getId(),
                        a.getActor(),
                        a.getAction(),
                        a.getTargetType(),
                        a.getTargetId(),
                        a.getDetails(),
                        a.getCreatedAt()
                ));
    }
}
