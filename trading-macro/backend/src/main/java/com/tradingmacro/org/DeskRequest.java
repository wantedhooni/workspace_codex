package com.tradingmacro.org;

import jakarta.validation.constraints.NotBlank;

public record DeskRequest(
    @NotBlank String name,
    Long teamId
) {}
