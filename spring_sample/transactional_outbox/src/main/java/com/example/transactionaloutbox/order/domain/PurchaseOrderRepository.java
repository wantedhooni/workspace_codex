package com.example.transactionaloutbox.order.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {

    List<PurchaseOrder> findTop20ByOrderByCreatedAtDesc();
}
