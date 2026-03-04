package com.revy.scaffolding.order.domain.repository;

import com.revy.scaffolding.order.dto.OrderSummaryResponse;
import java.util.List;

public interface PurchaseOrderQueryRepository {
    List<OrderSummaryResponse> findByUserId(Long userId);
}

