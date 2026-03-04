package com.example.r2dbcauditlog.support;

import com.example.r2dbcauditlog.auditlog.ApprovalRequestNotFoundException;
import com.example.r2dbcauditlog.auditlog.DuplicateRequestNumberException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApprovalRequestNotFoundException.class)
    public ProblemDetail handleNotFound(ApprovalRequestNotFoundException exception) {
        return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateRequestNumberException.class)
    public ProblemDetail handleDuplicate(DuplicateRequestNumberException exception) {
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

    private ProblemDetail createProblemDetail(HttpStatus status, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        return problemDetail;
    }
}
