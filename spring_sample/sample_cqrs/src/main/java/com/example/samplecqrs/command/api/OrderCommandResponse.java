package com.example.samplecqrs.command.api;

import com.example.samplecqrs.command.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 명령 처리 결과를 호출자에게 반환하는 응답 모델이다.
 *
 * @param orderId 주문 식별자
 * @param customerId 고객 식별자
 * @param productCode 상품 코드
 * @param quantity 주문 수량
 * @param unitPrice 단가
 * @param totalAmount 총액
 * @param status 주문 상태
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 */
public record OrderCommandResponse(
        String orderId,
        String customerId,
        String productCode,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
