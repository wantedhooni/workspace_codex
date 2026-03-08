package com.example.samplecqrs.query.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSummaryViewRepository extends JpaRepository<OrderSummaryView, String> {

    List<OrderSummaryView> findTop20ByOrderByCreatedAtDesc();

    List<OrderSummaryView> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
