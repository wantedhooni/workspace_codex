package com.revy.scaffolding.order.domain.repository;

import com.revy.scaffolding.order.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, PurchaseOrderQueryRepository {
}

