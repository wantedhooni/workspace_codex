package com.derivops.mvp.batch.infrastructure;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.application.*;
import com.derivops.mvp.batch.dto.*;
import com.derivops.mvp.batch.config.*;
import com.derivops.mvp.batch.job.*;


import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRunRepository extends JpaRepository<BatchRun, Long>, BatchRunRepositoryCustom {
    Page<BatchRun> findByStartedAtBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    Page<BatchRun> findByStatus(BatchStatus status, Pageable pageable);

    Page<BatchRun> findByStatusAndStartedAtBetween(BatchStatus status, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
