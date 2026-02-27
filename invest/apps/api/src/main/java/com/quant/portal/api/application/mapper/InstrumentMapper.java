package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.instrument.InstrumentCreateRequest;
import com.quant.portal.api.presentation.dto.instrument.InstrumentResponse;
import com.quant.portal.domain.portfolio.entity.Instrument;

public final class InstrumentMapper {

    private InstrumentMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static Instrument toEntity(InstrumentCreateRequest request) {
        if (request == null) {
            return null;
        }
        return new Instrument(
                request.ticker(),
                request.name(),
                request.marketCode(),
                request.currencyCode()
        );
    }

    public static InstrumentResponse toDto(Instrument instrument) {
        if (instrument == null) {
            return null;
        }
        return new InstrumentResponse(
                instrument.getId(),
                instrument.getTicker(),
                instrument.getName(),
                instrument.getMarketCode(),
                instrument.getCurrencyCode()
        );
    }
}
