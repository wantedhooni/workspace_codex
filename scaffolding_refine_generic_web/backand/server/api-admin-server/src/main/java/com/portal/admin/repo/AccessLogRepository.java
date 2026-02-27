package com.portal.admin.repo;

import com.portal.admin.domain.AccessLog;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long>, SearchableRepository<AccessLog>, FieldSearchableRepository<AccessLog>, AccessLogStatisticsRepository {}
