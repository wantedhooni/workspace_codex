package com.curd.template.core.error;

import java.util.List;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final String orgCode;
    private final String appCode;
    private final HttpStatus status;
    private final List<ApiErrorItem> errors;

    public ApiException(AppErrorCode errorCode, String message) {
        this(errorCode, message, List.of());
    }

    public ApiException(AppErrorCode errorCode, String message, List<ApiErrorItem> errors) {
        super(message);
        this.orgCode = errorCode.orgCode();
        this.appCode = errorCode.appCode();
        this.status = errorCode.status();
        this.errors = errors;
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

    public List<ApiErrorItem> errors() {
        return errors;
    }
}
