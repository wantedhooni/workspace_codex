package com.revy.scaffolding.order.exception;

import com.revy.scaffolding.core.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UpstreamUserServiceException extends BusinessException {
    public UpstreamUserServiceException() {
        super("UPSTREAM_USER_SERVICE_ERROR", "user-service 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY);
    }
}

