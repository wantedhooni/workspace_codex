package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.macroindicator.MacroIndicatorCreateRequest;
import com.quant.portal.api.presentation.dto.macroindicator.MacroIndicatorResponse;
import com.quant.portal.domain.macro.entity.MacroIndicator;

public final class MacroIndicatorMapper {

    private MacroIndicatorMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static MacroIndicator toEntity(MacroIndicatorCreateRequest request) {
        if (request == null) {
            return null;
        }

        return new MacroIndicator(
                request.indicatorCode(),
                request.indicatorName(),
                request.regionCode(),
                request.observedDate(),
                request.indicatorValue(),
                request.unit(),
                request.source()
        );
    }

    public static MacroIndicatorResponse toDto(MacroIndicator indicator) {
        if (indicator == null) {
            return null;
        }

        return new MacroIndicatorResponse(
                indicator.getId(),
                indicator.getIndicatorCode(),
                indicator.getIndicatorName(),
                indicator.getRegionCode(),
                indicator.getObservedDate(),
                indicator.getIndicatorValue(),
                indicator.getUnit(),
                indicator.getSource()
        );
    }
}
