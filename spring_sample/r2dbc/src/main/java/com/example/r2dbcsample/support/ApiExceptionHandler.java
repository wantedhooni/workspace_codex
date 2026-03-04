package com.example.r2dbcsample.support;

import com.example.r2dbcsample.customer.DuplicateCustomerCodeException;
import com.example.r2dbcsample.customer.CustomerNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleCustomerNotFound(CustomerNotFoundException exception) {
        return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateCustomerCodeException.class)
    public ProblemDetail handleDuplicateCustomerCode(DuplicateCustomerCodeException exception) {
        return createProblemDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidation(WebExchangeBindException exception) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.");
        problemDetail.setProperty(
                "errors",
                exception.getFieldErrors().stream()
                        .map(fieldError -> Map.of(
                                "field", fieldError.getField(),
                                "message", fieldError.getDefaultMessage()
                        ))
                        .toList()
        );
        return problemDetail;
    }

    private ProblemDetail createProblemDetail(HttpStatus httpStatus, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, detail);
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        return problemDetail;
    }
}
