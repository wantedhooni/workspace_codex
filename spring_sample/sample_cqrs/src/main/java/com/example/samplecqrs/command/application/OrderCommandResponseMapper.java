package com.example.samplecqrs.command.application;

import com.example.samplecqrs.command.api.OrderCommandResponse;
import com.example.samplecqrs.command.domain.PurchaseOrder;

/**
 * 주문 애그리게이트를 명령 응답 DTO로 변환한다.
 */
public final class OrderCommandResponseMapper {

    private OrderCommandResponseMapper() {
    }

    /**
     * 도메인 주문 객체를 API 응답으로 변환한다.
     *
     * @param order 주문 애그리게이트
     * @return 명령 응답 DTO
     */
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
