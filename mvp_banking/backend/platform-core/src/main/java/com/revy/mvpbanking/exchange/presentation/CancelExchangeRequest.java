package com.revy.mvpbanking.exchange.presentation;

import jakarta.validation.constraints.Size;

public record CancelExchangeRequest(
        @Size(max = 255) String reason
) {
}
