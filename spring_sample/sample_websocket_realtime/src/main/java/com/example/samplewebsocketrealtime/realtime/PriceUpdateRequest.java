package com.example.samplewebsocketrealtime.realtime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record PriceUpdateRequest(
        @NotBlank String instrument,
        @DecimalMin("0.01") BigDecimal price
) {
}
