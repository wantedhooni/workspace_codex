package com.derivops.mvp.stockposition.infrastructure;

import com.derivops.mvp.stockposition.StockPosition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPositionRepository extends JpaRepository<StockPosition, Long>, StockPositionRepositoryCustom {
    Optional<StockPosition> findByAccountIdAndSymbol(Long accountId, String symbol);

    List<StockPosition> findByAccountIdOrderByTotalCostDescIdAsc(Long accountId);
}
