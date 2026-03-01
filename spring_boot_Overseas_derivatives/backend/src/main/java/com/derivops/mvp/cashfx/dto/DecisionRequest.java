package com.derivops.mvp.cashfx.dto;
import com.derivops.mvp.cashfx.*;
import com.derivops.mvp.cashfx.api.*;
import com.derivops.mvp.cashfx.application.*;
import com.derivops.mvp.cashfx.infrastructure.*;


import jakarta.validation.constraints.NotBlank;

public record DecisionRequest(@NotBlank String reason) {
}
