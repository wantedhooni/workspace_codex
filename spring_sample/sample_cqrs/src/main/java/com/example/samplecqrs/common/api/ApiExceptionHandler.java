package com.example.samplecqrs.common.api;

import com.example.samplecqrs.command.application.OrderNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * CQRS 샘플 API에서 발생한 예외를 공통 응답 형식으로 변환한다.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 주문 미존재 예외를 404 응답으로 변환한다.
     *
     * @param exception 미존재 예외
     * @return 표준 오류 응답
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(OrderNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(exception.getMessage(), Instant.now()));
    }

    /**
     * 요청 검증 실패를 400 응답으로 변환한다.
     *
     * @param exception 검증 예외
     * @return 표준 오류 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("요청 검증에 실패했습니다.");
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(message, Instant.now()));
    }
}
