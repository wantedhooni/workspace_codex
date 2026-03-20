package com.example.observability.web.dto;

import java.math.BigDecimal;

public record DashboardResponse(
        String tenantId,
        long pendingCount,
        long inProgressCount,
        long completedCount,
        long failedCount,
        BigDecimal totalAmount
) {
}
