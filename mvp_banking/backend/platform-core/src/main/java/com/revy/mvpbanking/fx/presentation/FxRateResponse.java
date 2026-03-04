package com.revy.mvpbanking.fx.presentation;

import com.revy.mvpbanking.fx.domain.FxRate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FxRateResponse(
        UUID id,
        String baseCurrency,
        String quoteCurrency,
        BigDecimal rate,
        Instant effectiveAt,
        String source
) {
    public static FxRateResponse from(FxRate fxRate) {
        return new FxRateResponse(
                fxRate.getId(),
                fxRate.getBaseCurrency(),
                fxRate.getQuoteCurrency(),
                fxRate.getRate(),
                fxRate.getEffectiveAt(),
                fxRate.getSource()
        );
    }
}
