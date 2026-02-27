package com.curd.template.core.error;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(AppErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
