package com.quant.portal.api.presentation.dto.macroindicator;

import com.quant.portal.domain.macro.enums.MacroRegionCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MacroIndicatorUpdateRequest(
        @NotBlank String indicatorName,
        @NotNull MacroRegionCode regionCode,
        @NotNull LocalDate observedDate,
        @NotNull BigDecimal indicatorValue,
        String unit,
        String source
) {
}
