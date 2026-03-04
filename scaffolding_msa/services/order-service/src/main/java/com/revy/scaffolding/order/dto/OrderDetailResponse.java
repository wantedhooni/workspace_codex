package com.revy.scaffolding.order.dto;

import com.revy.scaffolding.order.domain.OrderStatus;
import com.revy.scaffolding.order.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDetailResponse(
    Long id,
    String orderNo,
    Long userId,
    String userName,
    BigDecimal amount,
    OrderStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static OrderDetailResponse of(PurchaseOrder order, UserLookupResponse user) {
        return new OrderDetailResponse(
            order.getId(),
            order.getOrderNo(),
            order.getUserId(),
            user.name(),
            order.getAmount(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}

