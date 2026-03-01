package com.derivops.mvp.stockpurchase.infrastructure;

import com.derivops.mvp.stockpurchase.StockPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockPurchaseRepositoryCustom {
    Page<StockPurchase> search(Long accountId, String symbol, String keyword, String filter, Pageable pageable);
}
