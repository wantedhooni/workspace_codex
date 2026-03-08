package com.example.samplecqrs.command.domain;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 쓰기 모델을 저장하고 조회하는 저장소다.
 */
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
}
