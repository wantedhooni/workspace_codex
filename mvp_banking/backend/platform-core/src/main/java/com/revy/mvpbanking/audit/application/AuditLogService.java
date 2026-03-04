package com.revy.mvpbanking.audit.application;

import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.audit.domain.AuditActorType;
import com.revy.mvpbanking.audit.domain.AuditLog;
import com.revy.mvpbanking.audit.domain.AuditLogRepository;
import com.revy.mvpbanking.auth.infrastructure.AuthUserPrincipal;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            CurrentPrincipalProvider currentPrincipalProvider
    ) {
        this.auditLogRepository = auditLogRepository;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @Transactional
    public void logSystem(AuditActionType actionType, String targetType, String targetId, String description) {
        save(AuditActorType.SYSTEM, null, "system", actionType, targetType, targetId, description);
    }

    @Transactional
    public void logCurrentActor(AuditActionType actionType, String targetType, String targetId, String description) {
        AuthUserPrincipal principal = currentPrincipalProvider.getCurrentPrincipal();
        save(
                principal.getPrincipalType().name().equals("ADMIN") ? AuditActorType.ADMIN : AuditActorType.USER,
                principal.getPrincipalId(),
                principal.getUsername(),
                actionType,
                targetType,
                targetId,
                description
        );
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findAllByOrderByLoggedAtDesc();
    }

    private void save(
            AuditActorType actorType,
            java.util.UUID actorId,
            String actorEmail,
            AuditActionType actionType,
            String targetType,
            String targetId,
            String description
    ) {
        auditLogRepository.save(new AuditLog(
                actorType,
                actorId,
                actorEmail,
                actionType,
                targetType,
                targetId,
                description,
                Instant.now()
        ));
    }
}
