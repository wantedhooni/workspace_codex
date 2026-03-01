package com.derivops.mvp.batch.infrastructure;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.application.*;
import com.derivops.mvp.batch.dto.*;
import com.derivops.mvp.batch.config.*;
import com.derivops.mvp.batch.job.*;


import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BatchRunRepositoryCustom {
    Page<BatchRun> search(LocalDate date, BatchStatus status, String keyword, String filter, Pageable pageable);
}
