package com.example.r2dbcauditlog.auditlog;

public class ApprovalRequestNotFoundException extends RuntimeException {

    public ApprovalRequestNotFoundException(String requestNumber) {
        super("승인 요청을 찾을 수 없습니다. requestNumber=" + requestNumber);
    }
}
