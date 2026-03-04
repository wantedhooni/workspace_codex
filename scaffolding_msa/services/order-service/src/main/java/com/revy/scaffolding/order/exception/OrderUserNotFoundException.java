package com.revy.scaffolding.order.exception;

import com.revy.scaffolding.core.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OrderUserNotFoundException extends BusinessException {
    public OrderUserNotFoundException(Long userId) {
        super("ORDER_USER_NOT_FOUND", "주문 대상 사용자를 찾을 수 없습니다. userId=" + userId, HttpStatus.NOT_FOUND);
    }
}

