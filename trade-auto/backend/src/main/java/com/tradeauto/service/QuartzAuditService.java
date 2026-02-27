package com.tradeauto.service;

import com.tradeauto.model.QuartzAudit;
import com.tradeauto.repo.QuartzAuditRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuartzAuditService {
    private final QuartzAuditRepository repository;

    public QuartzAuditService(QuartzAuditRepository repository) {
        this.repository = repository;
    }

    public void log(String action, String target, String detail) {
        QuartzAudit audit = new QuartzAudit();
        audit.setAction(action);
        audit.setTarget(target);
        audit.setDetail(detail);
        audit.setActor(getActor());
        repository.save(audit);
    }

    public List<QuartzAudit> recent() {
        return repository.findTop100ByOrderByCreatedAtDesc();
    }

    private String getActor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return "unknown";
        }
        return auth.getName();
    }
}
