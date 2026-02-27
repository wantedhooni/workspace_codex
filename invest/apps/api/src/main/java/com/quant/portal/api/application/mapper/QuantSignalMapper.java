package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalCreateRequest;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalExecutionResponse;
import com.quant.portal.api.presentation.dto.quantsignal.QuantSignalResponse;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.quant.entity.QuantSignal;
import com.quant.portal.domain.quant.entity.QuantSignalExecution;
import com.quant.portal.domain.quant.entity.QuantStrategy;

public final class QuantSignalMapper {

    private QuantSignalMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static QuantSignal toEntity(
            QuantSignalCreateRequest request,
            QuantStrategy strategy,
            Instrument instrument
    ) {
        if (request == null) {
            return null;
        }

        return new QuantSignal(
                strategy,
                instrument,
                request.signalType(),
                request.signalDate(),
                request.score(),
                request.confidence(),
                request.rationale()
        );
    }

    public static QuantSignalResponse toDto(QuantSignal signal) {
        if (signal == null) {
            return null;
        }

        return new QuantSignalResponse(
                signal.getId(),
                signal.getStrategy().getId(),
                signal.getStrategy().getName(),
                signal.getInstrument().getId(),
                signal.getInstrument().getTicker(),
                signal.getSignalType(),
                signal.getSignalDate(),
                signal.getScore(),
                signal.getConfidence(),
                signal.getRationale()
        );
    }

    public static QuantSignalExecutionResponse toExecutionDto(QuantSignalExecution execution) {
        if (execution == null) {
            return null;
        }

        return new QuantSignalExecutionResponse(
                execution.getId(),
                execution.getSignal().getId(),
                execution.getTransaction().getId(),
                execution.getCreatedAt(),
                execution.getCreatedBy()
        );
    }
}
