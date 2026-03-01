package com.derivops.mvp.audit.application;
import com.derivops.mvp.audit.*;
import com.derivops.mvp.audit.api.*;
import com.derivops.mvp.audit.dto.*;
import com.derivops.mvp.audit.infrastructure.*;


import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RequiredArgsConstructor
@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogR2dbcWriter auditLogR2dbcWriter;

    public void log(String actor, String action, String targetType, String targetId, String details) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    auditLogR2dbcWriter.insert(actor, action, targetType, targetId, details);
                }
            });
            return;
        }

        auditLogR2dbcWriter.insert(actor, action, targetType, targetId, details);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<AuditLogResponse> search(
            String actor,
            String action,
            String keyword,
            String filter,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        return auditLogRepository.search(actor, action, keyword, filter, from, to, pageable)
                .map(a -> new AuditLogResponse(
                        a.getId(),
                        a.getActor(),
                        a.getAction(),
                        a.getTargetType(),
                        a.getTargetId(),
                        a.getDetails(),
                        a.getCreatedAt()
                ));
    }
}
