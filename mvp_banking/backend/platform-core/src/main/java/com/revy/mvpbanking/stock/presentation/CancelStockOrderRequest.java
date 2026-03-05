package com.revy.mvpbanking.stock.presentation;

import jakarta.validation.constraints.Size;

public record CancelStockOrderRequest(
        @Size(max = 255) String reason
) {
}
