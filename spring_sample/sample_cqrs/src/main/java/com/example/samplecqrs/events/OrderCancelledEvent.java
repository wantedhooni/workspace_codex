package com.example.samplecqrs.events;

import com.example.samplecqrs.command.domain.OrderStatus;
import com.example.samplecqrs.command.domain.PurchaseOrder;
import java.time.Instant;

/**
 * 주문 취소 후 조회 프로젝션 갱신에 사용하는 도메인 이벤트다.
 *
 * @param orderId 주문 식별자
 * @param status 주문 상태
 * @param occurredAt 이벤트 발생 시각
 */
public record OrderCancelledEvent(
        String orderId,
        OrderStatus status,
        Instant occurredAt
) {

    /**
     * 주문 애그리게이트를 취소 이벤트로 변환한다.
     *
     * @param order 주문 애그리게이트
     * @return 취소 이벤트
     */
    public static OrderCancelledEvent from(PurchaseOrder order) {
        return new OrderCancelledEvent(order.getId(), order.getStatus(), Instant.now());
    }
}
