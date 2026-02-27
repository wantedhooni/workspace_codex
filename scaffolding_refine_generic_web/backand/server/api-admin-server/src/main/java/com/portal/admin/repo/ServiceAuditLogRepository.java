package com.portal.admin.repo;

import com.portal.admin.domain.ServiceAuditLog;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceAuditLogRepository extends JpaRepository<ServiceAuditLog, Long>, SearchableRepository<ServiceAuditLog>, FieldSearchableRepository<ServiceAuditLog> {}
