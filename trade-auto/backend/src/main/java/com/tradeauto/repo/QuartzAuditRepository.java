package com.tradeauto.repo;

import com.tradeauto.model.QuartzAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuartzAuditRepository extends JpaRepository<QuartzAudit, Long> {
    List<QuartzAudit> findTop100ByOrderByCreatedAtDesc();
}
