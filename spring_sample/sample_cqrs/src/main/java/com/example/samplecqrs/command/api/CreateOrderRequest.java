package com.example.samplecqrs.command.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 주문 생성 명령에 필요한 입력 값을 담는 요청 모델이다.
 *
 * @param customerId 고객 식별자
 * @param productCode 상품 코드
 * @param quantity 주문 수량
 * @param unitPrice 단가
 */
public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotBlank String productCode,
        @Min(1) int quantity,
        @DecimalMin(value = "0.01") BigDecimal unitPrice
) {
}
