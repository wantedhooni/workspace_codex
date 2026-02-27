package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.quantstrategy.QuantStrategyCreateRequest;
import com.quant.portal.api.presentation.dto.quantstrategy.QuantStrategyResponse;
import com.quant.portal.domain.quant.entity.QuantStrategy;
import com.quant.portal.domain.quant.enums.StrategyStatus;

public final class QuantStrategyMapper {

    private static final int DEFAULT_REBALANCE_CYCLE_DAYS = 20;

    private QuantStrategyMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static QuantStrategy toEntity(QuantStrategyCreateRequest request) {
        if (request == null) {
            return null;
        }

        StrategyStatus status = request.status() == null ? StrategyStatus.DRAFT : request.status();
        Integer rebalanceCycleDays = request.rebalanceCycleDays() == null
                ? DEFAULT_REBALANCE_CYCLE_DAYS
                : request.rebalanceCycleDays();

        return new QuantStrategy(
                request.name(),
                request.style(),
                status,
                rebalanceCycleDays,
                request.description()
        );
    }

    public static QuantStrategyResponse toDto(QuantStrategy strategy) {
        if (strategy == null) {
            return null;
        }

        return new QuantStrategyResponse(
                strategy.getId(),
                strategy.getName(),
                strategy.getStyle(),
                strategy.getStatus(),
                strategy.getRebalanceCycleDays(),
                strategy.getDescription()
        );
    }
}
