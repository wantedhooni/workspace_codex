package com.derivops.mvp.opscase.infrastructure;
import com.derivops.mvp.opscase.*;
import com.derivops.mvp.opscase.api.*;
import com.derivops.mvp.opscase.application.*;
import com.derivops.mvp.opscase.dto.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OpsCaseRepositoryCustom {
    Page<OpsCase> search(
            OpsCaseStatus status,
            OpsCaseSeverity severity,
            String assignee,
            String keyword,
            String filter,
            Pageable pageable
    );
}
