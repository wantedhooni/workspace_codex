package com.revy.mvpbanking.stock.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockOrderRepository extends JpaRepository<StockOrder, UUID> {
    List<StockOrder> findAllByOrderByCreatedAtDesc();
    List<StockOrder> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<StockOrder> findByOrderNumber(String orderNumber);
}
