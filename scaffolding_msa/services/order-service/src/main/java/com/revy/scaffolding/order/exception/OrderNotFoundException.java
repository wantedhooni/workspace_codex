package com.revy.scaffolding.order.exception;

import com.revy.scaffolding.core.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends BusinessException {
    public OrderNotFoundException(Long orderId) {
        super("ORDER_NOT_FOUND", "주문을 찾을 수 없습니다. id=" + orderId, HttpStatus.NOT_FOUND);
    }
}

