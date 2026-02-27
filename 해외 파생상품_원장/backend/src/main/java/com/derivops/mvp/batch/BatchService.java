package com.derivops.mvp.batch;

import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.common.rsql.RsqlSpecificationBuilder;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BatchService {
    private static final Map<String, String> BATCH_FILTER_FIELDS = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("batchName", "batchName"),
            Map.entry("status", "status"),
            Map.entry("startedAt", "startedAt"),
            Map.entry("finishedAt", "finishedAt"),
            Map.entry("errorMessage", "errorMessage"),
            Map.entry("retryCount", "retryCount")
    );

    private final BatchRunRepository batchRunRepository;

    public Page<BatchRunResponse> list(LocalDate date, BatchStatus status, String keyword, String filter, Pageable pageable) {
        Specification<BatchRun> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (date != null) {
                OffsetDateTime from = date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
                OffsetDateTime to = date.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
                predicates.add(cb.greaterThanOrEqualTo(root.get("startedAt"), from));
                predicates.add(cb.lessThan(root.get("startedAt"), to));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String q = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("batchName")), q),
                        cb.like(cb.lower(root.get("errorMessage")), q)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        if (filter != null && !filter.isBlank()) {
            spec = spec.and(RsqlSpecificationBuilder.build(filter, BATCH_FILTER_FIELDS));
        }

        return batchRunRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public BatchRunResponse get(Long runId) {
        BatchRun run = batchRunRepository.findById(runId)
                .orElseThrow(() -> new NotFoundException("Batch run not found: " + runId));
        return toResponse(run);
    }

    private BatchRunResponse toResponse(BatchRun run) {
        return new BatchRunResponse(
                run.getId(),
                run.getBatchName(),
                run.getStatus(),
                run.getStartedAt(),
                run.getFinishedAt(),
                run.getErrorMessage(),
                run.getRetryCount()
        );
    }
}
