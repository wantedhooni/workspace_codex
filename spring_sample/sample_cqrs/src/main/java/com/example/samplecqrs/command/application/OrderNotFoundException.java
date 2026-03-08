package com.example.samplecqrs.command.application;

/**
 * 존재하지 않는 주문을 조회하거나 변경하려 할 때 발생한다.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderId) {
        super("주문을 찾을 수 없습니다. orderId=" + orderId);
    }
}
