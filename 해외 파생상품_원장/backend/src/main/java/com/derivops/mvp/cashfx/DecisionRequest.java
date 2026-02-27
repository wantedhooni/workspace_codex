package com.derivops.mvp.cashfx;

import jakarta.validation.constraints.NotBlank;

public record DecisionRequest(@NotBlank String reason) {
}
