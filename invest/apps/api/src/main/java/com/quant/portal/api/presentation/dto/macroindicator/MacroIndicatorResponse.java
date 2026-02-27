package com.quant.portal.api.presentation.dto.macroindicator;

import com.quant.portal.domain.macro.enums.MacroRegionCode;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MacroIndicatorResponse(
        Long id,
        String indicatorCode,
        String indicatorName,
        MacroRegionCode regionCode,
        LocalDate observedDate,
        BigDecimal indicatorValue,
        String unit,
        String source
) {
}
