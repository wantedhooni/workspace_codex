package com.example.multitenancy.web.dto;

import com.example.multitenancy.domain.ProjectStatus;
import java.time.Instant;
import java.util.List;

/**
 * 데모 대시보드에 필요한 테넌트 요약과 프로젝트 목록을 담는다.
 */
public record DashboardResponse(
        TenantSummary tenant,
        List<ProjectItem> projects,
        List<StatusCount> statusCounts
) {

    public record TenantSummary(
            String tenantId,
            String displayName,
            String role
    ) {
    }

    public record ProjectItem(
            Long id,
            String name,
            String description,
            String ownerName,
            ProjectStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record StatusCount(
            ProjectStatus status,
            long count
    ) {
    }
}
