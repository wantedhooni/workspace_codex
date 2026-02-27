package com.quant.portal.api.presentation.dto.quantsignal;

import com.quant.portal.domain.quant.enums.SignalType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record QuantSignalResponse(
        Long id,
        Long strategyId,
        String strategyName,
        Long instrumentId,
        String ticker,
        SignalType signalType,
        LocalDate signalDate,
        BigDecimal score,
        BigDecimal confidence,
        String rationale
) {
}
