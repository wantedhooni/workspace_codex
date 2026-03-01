package com.derivops.mvp.stockpurchase.infrastructure;

import com.derivops.mvp.stockpurchase.StockPurchase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPurchaseRepository extends JpaRepository<StockPurchase, Long>, StockPurchaseRepositoryCustom {

    List<StockPurchase> findTop10ByAccountIdOrderByTradeDateDescIdDesc(Long accountId);
}
