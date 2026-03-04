package com.revy.mvpbanking.stock.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOrderExecutionRepository extends JpaRepository<StockOrderExecution, UUID> {
    List<StockOrderExecution> findByOrderIdInOrderByExecutedAtDesc(List<UUID> orderIds);
    List<StockOrderExecution> findByOrderIdOrderByExecutedAtDesc(UUID orderId);
    Optional<StockOrderExecution> findByExecutionNumber(String executionNumber);
}
