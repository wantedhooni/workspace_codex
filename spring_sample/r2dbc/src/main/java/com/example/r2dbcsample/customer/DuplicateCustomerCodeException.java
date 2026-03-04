package com.example.r2dbcsample.customer;

public class DuplicateCustomerCodeException extends RuntimeException {

    public DuplicateCustomerCodeException(String customerCode) {
        super("이미 사용 중인 고객 코드입니다. customerCode=" + customerCode);
    }
}
