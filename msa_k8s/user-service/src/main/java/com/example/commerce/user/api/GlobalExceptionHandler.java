package com.example.commerce.user.api;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.commerce.user.domain.DuplicateEmailException;
import com.example.commerce.user.domain.UserNotFoundException;

/**
 * User Service API에서 발생한 예외를 일관된 오류 응답으로 변환한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final Clock clock;

    /**
     * 오류 발생 시각을 생성할 시계를 주입받는다.
     *
     * @param clock 시스템 시계
     */
    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    /**
     * 요청 필드 검증 실패를 400 오류로 변환한다.
     *
     * @param exception 검증 예외
     * @param request HTTP 요청
     * @return 필드 오류를 포함한 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "요청값이 올바르지 않습니다.",
                request,
                fieldErrors);
    }

    /**
     * 이메일 중복을 409 충돌 오류로 변환한다.
     *
     * @param exception 이메일 중복 예외
     * @param request HTTP 요청
     * @return 충돌 오류 응답
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", exception.getMessage(), request, Map.of());
    }

    /**
     * 존재하지 않는 회원 조회를 404 오류로 변환한다.
     *
     * @param exception 회원 미존재 예외
     * @param request HTTP 요청
     * @return 미존재 오류 응답
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            UserNotFoundException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    /**
     * 처리하지 못한 예외를 기록하고 내부 정보가 노출되지 않는 500 오류로 변환한다.
     *
     * @param exception 처리하지 못한 예외
     * @param request HTTP 요청
     * @return 일반화된 서버 오류 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("처리하지 못한 API 오류가 발생했습니다.", exception);
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "요청 처리 중 오류가 발생했습니다.",
                request,
                Map.of());
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(clock),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                request.getHeader(CORRELATION_ID_HEADER),
                fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}

