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
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String actor, String action, String targetType, String targetId, String details) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
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
