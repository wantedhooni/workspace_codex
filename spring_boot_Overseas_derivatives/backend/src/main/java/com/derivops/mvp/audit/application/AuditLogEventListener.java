package com.derivops.mvp.audit.application;

import com.derivops.mvp.audit.infrastructure.AuditLogR2dbcWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class AuditLogEventListener {

    private final AuditLogR2dbcWriter auditLogR2dbcWriter;

    @Async("auditLogTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(AuditLogRequestedEvent event) {
        auditLogR2dbcWriter.insert(
                event.actor(),
                event.action(),
                event.targetType(),
                event.targetId(),
                event.details()
        );
    }
}
