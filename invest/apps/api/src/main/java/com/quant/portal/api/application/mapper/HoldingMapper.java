package com.quant.portal.api.application.mapper;

import com.quant.portal.api.presentation.dto.holding.HoldingResponse;
import com.quant.portal.domain.portfolio.entity.Holding;
import java.util.List;

public final class HoldingMapper {

    private HoldingMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static HoldingResponse toDto(Holding holding) {
        if (holding == null) {
            return null;
        }
        return new HoldingResponse(
                holding.getId(),
                holding.getPortfolio().getId(),
                holding.getInstrument().getId(),
                holding.getInstrument().getTicker(),
                holding.getQuantity(),
                holding.getAverageCost()
        );
    }

    public static List<HoldingResponse> toDtoList(List<Holding> holdings) {
        if (holdings == null) {
            return List.of();
        }
        return holdings.stream()
                .map(HoldingMapper::toDto)
                .toList();
    }
}
