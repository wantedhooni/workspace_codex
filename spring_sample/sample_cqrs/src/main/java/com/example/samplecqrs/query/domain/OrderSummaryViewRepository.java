package com.example.samplecqrs.query.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 조회 프로젝션을 위한 저장소다.
 */
public interface OrderSummaryViewRepository extends JpaRepository<OrderSummaryView, String> {

    List<OrderSummaryView> findTop20ByOrderByCreatedAtDesc();

    List<OrderSummaryView> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
