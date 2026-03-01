package com.derivops.mvp.stockposition.application;

import com.derivops.mvp.stockposition.StockPosition;
import com.derivops.mvp.stockposition.dto.StockPositionResponse;
import com.derivops.mvp.stockposition.infrastructure.StockPositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StockPositionService {

    private final StockPositionRepository stockPositionRepository;

    @Transactional(readOnly = true)
    public Page<StockPositionResponse> list(Long accountId, String symbol, String filter, Pageable pageable) {
        return stockPositionRepository.search(accountId, symbol, filter, pageable).map(this::toResponse);
    }

    private StockPositionResponse toResponse(StockPosition item) {
        return new StockPositionResponse(
                item.getId(),
                item.getAccount().getId(),
                item.getAccount().getAccountNo(),
                item.getSymbol(),
                item.getMarket(),
                item.getCurrency(),
                item.getQuantity(),
                item.getAveragePrice(),
                item.getTotalCost(),
                item.getLastTradeDate(),
                item.getUpdatedAt()
        );
    }
}
