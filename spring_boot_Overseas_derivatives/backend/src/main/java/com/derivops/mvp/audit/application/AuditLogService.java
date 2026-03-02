package com.derivops.mvp.audit.application;

import com.derivops.mvp.audit.dto.AuditLogResponse;
import com.derivops.mvp.audit.infrastructure.AuditLogRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void log(String actor, String action, String targetType, String targetId, String details) {
        applicationEventPublisher.publishEvent(new AuditLogRequestedEvent(
                actor,
                action,
                targetType,
                targetId,
                details
        ));
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
