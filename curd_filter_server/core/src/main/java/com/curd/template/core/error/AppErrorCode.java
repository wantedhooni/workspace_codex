package com.curd.template.core.error;

import org.springframework.http.HttpStatus;

public enum AppErrorCode {
    FILTER_PARSE_ERROR("CORE", "FILTER_PARSE_ERROR", HttpStatus.BAD_REQUEST),
    FILTER_POLICY_VIOLATION("CORE", "FILTER_POLICY_VIOLATION", HttpStatus.BAD_REQUEST),
    INVALID_SORT_FIELD("CORE", "INVALID_SORT_FIELD", HttpStatus.BAD_REQUEST),
    INVALID_PAGINATION("CORE", "INVALID_PAGINATION", HttpStatus.BAD_REQUEST),
    FILTER_NOT_ALLOWED("CORE", "FILTER_NOT_ALLOWED", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("CORE", "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND),
    CONFLICT("CORE", "CONFLICT", HttpStatus.CONFLICT),
    VALIDATION_ERROR("CORE", "VALIDATION_ERROR", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("CORE", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String orgCode;
    private final String appCode;
    private final HttpStatus status;

    AppErrorCode(String orgCode, String appCode, HttpStatus status) {
        this.orgCode = orgCode;
        this.appCode = appCode;
        this.status = status;
    }

    public String orgCode() {
        return orgCode;
    }

    public String appCode() {
        return appCode;
    }

    public HttpStatus status() {
        return status;
    }
}
