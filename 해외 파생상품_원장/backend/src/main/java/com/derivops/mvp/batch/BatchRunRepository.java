package com.derivops.mvp.batch;

import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BatchRunRepository extends JpaRepository<BatchRun, Long>, JpaSpecificationExecutor<BatchRun> {
    Page<BatchRun> findByStartedAtBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    Page<BatchRun> findByStatus(BatchStatus status, Pageable pageable);

    Page<BatchRun> findByStatusAndStartedAtBetween(BatchStatus status, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
