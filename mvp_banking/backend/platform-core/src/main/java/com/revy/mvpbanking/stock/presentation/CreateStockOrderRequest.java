package com.revy.mvpbanking.stock.presentation;

import com.revy.mvpbanking.stock.domain.StockOrderSide;
import com.revy.mvpbanking.stock.domain.StockOrderTimeInForce;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateStockOrderRequest(
        @NotNull UUID accountId,
        @NotBlank String symbol,
        @NotBlank String market,
        @NotNull StockOrderSide side,
        @NotNull @DecimalMin("0.0001") BigDecimal quantity,
        @NotNull @DecimalMin("0.0001") BigDecimal limitPrice,
        @NotBlank String currency,
        StockOrderTimeInForce timeInForce,
        @Size(max = 200) String orderMemo
) {
}
