package com.curd.template.api.error;

import com.curd.template.core.error.ApiErrorItem;
import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import com.curd.template.core.error.ProblemDetailsFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException exception) {
        return ProblemDetailsFactory.from(exception, traceId());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
        List<ApiErrorItem> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> new ApiErrorItem(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();

        ApiException apiException = new ApiException(AppErrorCode.VALIDATION_ERROR, "Validation failed", errors);
        return ProblemDetailsFactory.from(apiException, traceId());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error for path={}", request.getRequestURI(), exception);
        return ProblemDetailsFactory.fromUnexpected(exception, traceId());
    }

    private String traceId() {
        String traceId = MDC.get("traceId");
        return traceId == null ? "n/a" : traceId;
    }
}
