package com.example.marketsignal.common;

import org.springframework.http.HttpStatus;

/**
 * 도메인 규칙 위반을 표현하는 비즈니스 예외다.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public BusinessException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
