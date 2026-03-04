package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.stock.domain.StockOrderSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateStockOrderRequest(
        @NotNull UUID accountId,
        @NotBlank String symbol,
        @NotBlank String market,
        @NotNull StockOrderSide side,
        @NotNull @DecimalMin("0.0001") BigDecimal quantity,
        @NotNull @DecimalMin("0.0001") BigDecimal limitPrice,
        @NotBlank String currency
) {
}
