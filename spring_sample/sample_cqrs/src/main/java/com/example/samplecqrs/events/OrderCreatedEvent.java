package com.example.samplecqrs.events;

import com.example.samplecqrs.command.domain.OrderStatus;
import com.example.samplecqrs.command.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 주문 생성 후 조회 프로젝션 갱신에 사용하는 도메인 이벤트다.
 *
 * @param orderId 주문 식별자
 * @param customerId 고객 식별자
 * @param productCode 상품 코드
 * @param quantity 주문 수량
 * @param totalAmount 총액
 * @param status 주문 상태
 * @param occurredAt 이벤트 발생 시각
 */
public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String productCode,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant occurredAt
) {

    /**
     * 주문 애그리게이트를 생성 이벤트로 변환한다.
     *
     * @param order 주문 애그리게이트
     * @return 생성 이벤트
     */
    public static OrderCreatedEvent from(PurchaseOrder order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getProductCode(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
