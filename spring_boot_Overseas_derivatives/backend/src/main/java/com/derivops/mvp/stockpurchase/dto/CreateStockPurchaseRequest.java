package com.derivops.mvp.stockpurchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateStockPurchaseRequest(
        @NotNull Long accountId,
        @NotBlank String symbol,
        @NotBlank String market,
        @NotBlank String currency,
        @NotNull LocalDate tradeDate,
        LocalDate settlementDate,
        @NotNull @DecimalMin("0.0001") BigDecimal quantity,
        @NotNull @DecimalMin("0.0001") BigDecimal price,
        @NotNull @DecimalMin("0.0") BigDecimal feeAmount
) {
}
