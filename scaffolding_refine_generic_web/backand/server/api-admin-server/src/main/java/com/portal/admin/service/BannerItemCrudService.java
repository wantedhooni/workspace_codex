package com.portal.admin.service;

import com.portal.admin.domain.BannerItem;
import static com.portal.admin.dto.BannerItemDtos.*;
import com.portal.admin.repo.BannerItemRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class BannerItemCrudService extends BaseCrudService<BannerItem, Long, CreateBannerItemRequest, UpdateBannerItemRequest, BannerItemResponse> {

    public BannerItemCrudService(BannerItemRepository repository, ServiceAuditLogRepository auditLogs) {
        super(repository, auditLogs);
    }

    @Override
    protected BannerItem createEntity(CreateBannerItemRequest request) {
        return new BannerItem(
                request.title(),
                request.imageUrl(),
                request.linkUrl(),
                request.startAt(),
                request.endAt(),
                request.enabled() == null ? Boolean.TRUE : request.enabled(),
                request.sortOrder() == null ? 0 : request.sortOrder()
        );
    }

    @Override
    protected void applyUpdate(BannerItem entity, UpdateBannerItemRequest request) {
        if (request.title() != null) entity.setTitle(request.title());
        if (request.imageUrl() != null) entity.setImageUrl(request.imageUrl());
        if (request.linkUrl() != null) entity.setLinkUrl(request.linkUrl());
        if (request.startAt() != null) entity.setStartAt(request.startAt());
        if (request.endAt() != null) entity.setEndAt(request.endAt());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
        if (request.sortOrder() != null) entity.setSortOrder(request.sortOrder());
    }

    @Override
    protected BannerItemResponse toResponse(BannerItem entity) {
        return new BannerItemResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getImageUrl(),
                entity.getLinkUrl(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getEnabled(),
                entity.getSortOrder()
        );
    }
}
