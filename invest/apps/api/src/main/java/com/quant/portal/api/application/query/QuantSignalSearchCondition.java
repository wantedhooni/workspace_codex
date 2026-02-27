package com.quant.portal.api.application.query;

import com.quant.portal.domain.quant.enums.SignalType;
import java.time.LocalDate;

public record QuantSignalSearchCondition(
        Long strategyId,
        Long instrumentId,
        SignalType signalType,
        LocalDate fromDate,
        LocalDate toDate
) {
}
