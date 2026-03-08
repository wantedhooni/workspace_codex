package com.example.samplesaga.order.application;

public class SagaOrderNotFoundException extends RuntimeException {

    public SagaOrderNotFoundException(String orderId) {
        super("Saga 주문을 찾을 수 없습니다. orderId=" + orderId);
    }
}
