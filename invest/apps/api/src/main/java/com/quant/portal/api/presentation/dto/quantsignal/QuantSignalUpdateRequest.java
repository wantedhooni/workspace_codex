package com.quant.portal.api.presentation.dto.quantsignal;

import com.quant.portal.domain.quant.enums.SignalType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record QuantSignalUpdateRequest(
        @NotNull @Positive Long instrumentId,
        @NotNull SignalType signalType,
        @NotNull LocalDate signalDate,
        @NotNull BigDecimal score,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidence,
        String rationale
) {
}
