package com.derivops.mvp.stockposition.infrastructure;

import com.derivops.mvp.stockposition.StockPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockPositionRepositoryCustom {
    Page<StockPosition> search(Long accountId, String symbol, String filter, Pageable pageable);
}
