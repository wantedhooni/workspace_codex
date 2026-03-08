package com.example.samplecqrs.command.application;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super("주문을 찾을 수 없습니다. orderId=" + orderId);
    }
}
