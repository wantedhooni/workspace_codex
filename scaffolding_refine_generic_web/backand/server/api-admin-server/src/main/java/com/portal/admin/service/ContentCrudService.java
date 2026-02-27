package com.portal.admin.service;

import com.portal.admin.domain.ContentPage;
import static com.portal.admin.dto.ContentDtos.*;
import com.portal.admin.repo.ContentRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class ContentCrudService extends BaseCrudService<ContentPage, Long, CreateContentRequest, UpdateContentRequest, ContentResponse> {

    public ContentCrudService(ContentRepository contents, ServiceAuditLogRepository auditLogs) {
        super(contents, auditLogs);
    }

    @Override
    protected ContentPage createEntity(CreateContentRequest request) {
        return new ContentPage(request.title(), request.slug(), request.body(), request.status());
    }

    @Override
    protected void applyUpdate(ContentPage page, UpdateContentRequest request) {
        if (request.title() != null) page.setTitle(request.title());
        if (request.slug() != null) page.setSlug(request.slug());
        if (request.body() != null) page.setBody(request.body());
        if (request.status() != null) page.setStatus(request.status());
    }

    @Override
    protected ContentResponse toResponse(ContentPage page) {
        return new ContentResponse(page.getId(), page.getTitle(), page.getSlug(), page.getBody(), page.getStatus(), page.getUpdatedAt());
    }
}
