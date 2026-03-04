package com.revy.mvpbanking.stock.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPositionRepository extends JpaRepository<StockPosition, UUID> {
    List<StockPosition> findAllByOrderByCreatedAtDesc();
    List<StockPosition> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<StockPosition> findByAccountIdAndSymbolIgnoreCase(UUID accountId, String symbol);
}
