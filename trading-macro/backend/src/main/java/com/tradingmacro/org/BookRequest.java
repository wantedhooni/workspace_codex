package com.tradingmacro.org;

import jakarta.validation.constraints.NotBlank;

public record BookRequest(
    @NotBlank String name,
    Long deskId
) {}
