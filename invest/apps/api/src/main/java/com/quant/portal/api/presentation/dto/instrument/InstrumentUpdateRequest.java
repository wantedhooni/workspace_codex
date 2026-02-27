package com.quant.portal.api.presentation.dto.instrument;

import jakarta.validation.constraints.NotBlank;

public record InstrumentUpdateRequest(
        @NotBlank String name
) {
}
