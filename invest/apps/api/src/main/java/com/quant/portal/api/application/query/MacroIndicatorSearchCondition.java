package com.quant.portal.api.application.query;

import com.quant.portal.domain.macro.enums.MacroRegionCode;
import java.time.LocalDate;

public record MacroIndicatorSearchCondition(
        String keyword,
        MacroRegionCode regionCode,
        LocalDate fromDate,
        LocalDate toDate
) {
}
