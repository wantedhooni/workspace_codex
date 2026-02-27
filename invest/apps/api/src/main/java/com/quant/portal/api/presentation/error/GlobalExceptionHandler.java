package com.quant.portal.api.presentation.error;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorEnvelope> handleApiException(ApiException exception) {
        ApiErrorResponse response = buildError(
                exception.getErrorCode().name(),
                exception.getMessage(),
                exception.getDetails()
        );
        return ResponseEntity.status(exception.getStatus()).body(new ApiErrorEnvelope(response));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorEnvelope> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorResponse response = buildError(
                ErrorCode.BAD_REQUEST.name(),
                "Request validation failed",
                fieldErrors
        );
        return ResponseEntity.badRequest().body(new ApiErrorEnvelope(response));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorEnvelope> handleUnexpectedException(Exception exception) {
        ApiErrorResponse response = buildError(
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "Unexpected error has occurred",
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorEnvelope(response));
    }

    private ApiErrorResponse buildError(String code, String message, Object details) {
        return new ApiErrorResponse(
                Instant.now(),
                code,
                message,
                details,
                MDC.get(TRACE_ID_KEY)
        );
    }
}
