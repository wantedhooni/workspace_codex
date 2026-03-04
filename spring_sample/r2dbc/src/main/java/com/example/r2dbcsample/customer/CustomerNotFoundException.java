package com.example.r2dbcsample.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerCode) {
        super("고객 코드를 찾을 수 없습니다. customerCode=" + customerCode);
    }
}
