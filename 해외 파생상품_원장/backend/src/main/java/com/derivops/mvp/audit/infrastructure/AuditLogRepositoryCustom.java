package com.derivops.mvp.audit.infrastructure;
import com.derivops.mvp.audit.*;
import com.derivops.mvp.audit.api.*;
import com.derivops.mvp.audit.application.*;
import com.derivops.mvp.audit.dto.*;


import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogRepositoryCustom {
    Page<AuditLog> search(String actor, String action, String keyword, String filter, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
