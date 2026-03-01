package com.derivops.mvp.audit.infrastructure;
import com.derivops.mvp.audit.*;
import com.derivops.mvp.audit.api.*;
import com.derivops.mvp.audit.application.*;
import com.derivops.mvp.audit.dto.*;


import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, AuditLogRepositoryCustom {
}
