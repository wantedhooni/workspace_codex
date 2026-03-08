package com.example.samplecqrs.command.application;

import com.example.samplecqrs.command.api.OrderCommandResponse;
import com.example.samplecqrs.command.domain.PurchaseOrder;

public final class OrderCommandResponseMapper {

    private OrderCommandResponseMapper() {
    }

    public static OrderCommandResponse toResponse(PurchaseOrder order) {
        return new OrderCommandResponse(
                order.getId(),
                order.getCustomerId(),
                order.getProductCode(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
