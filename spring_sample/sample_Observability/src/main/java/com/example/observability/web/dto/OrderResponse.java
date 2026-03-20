package com.example.observability.web.dto;

import com.example.observability.domain.CustomerOrder;
import com.example.observability.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderResponse(
        Long id,
        String tenantId,
        String orderNumber,
        String title,
        String customerName,
        OrderStatus status,
        String priority,
        BigDecimal amount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime dueAt
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getTenantId(),
                order.getOrderNumber(),
                order.getTitle(),
                order.getCustomerName(),
                order.getStatus(),
                order.getPriority(),
                order.getAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getDueAt()
        );
    }
}
