package com.example.observability.domain;

/**
 * 주문 운영 처리 상태를 정의한다.
 */
public enum OrderStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
