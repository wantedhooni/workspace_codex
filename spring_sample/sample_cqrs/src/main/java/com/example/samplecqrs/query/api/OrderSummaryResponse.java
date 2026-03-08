package com.example.samplecqrs.query.api;

import com.example.samplecqrs.command.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 조회 전용 프로젝션을 외부에 노출하는 응답 모델이다.
 *
 * @param orderId 주문 식별자
 * @param customerId 고객 식별자
 * @param productCode 상품 코드
 * @param quantity 주문 수량
 * @param totalAmount 총액
 * @param status 주문 상태
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 */
public record OrderSummaryResponse(
        String orderId,
        String customerId,
        String productCode,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
