package com.example.orderapp.common.domain;

public enum OutboxStatus {
    INIT,
    RETRYING,
    FAILED,
    PUBLISHED
}
