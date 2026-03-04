package com.example.r2dbcauditlog.auditlog;

public class DuplicateRequestNumberException extends RuntimeException {

    public DuplicateRequestNumberException(String requestNumber) {
        super("이미 사용 중인 요청 번호입니다. requestNumber=" + requestNumber);
    }
}
