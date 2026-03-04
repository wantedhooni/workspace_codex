package com.revy.scaffolding.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderCreateRequest(
    @NotNull(message = "사용자 ID는 필수입니다.")
    Long userId,
    @NotNull(message = "주문 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "주문 금액은 0보다 커야 합니다.")
    BigDecimal amount
) {
}

