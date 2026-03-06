package com.example.sampleaibot.chat.api;

import com.example.sampleaibot.chat.service.OllamaChatException;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ChatApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String details = exception.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        ApiErrorResponse errorResponse = new ApiErrorResponse(
            "VALIDATION_ERROR",
            details,
            Instant.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(OllamaChatException.class)
    public ResponseEntity<ApiErrorResponse> handleOllama(OllamaChatException exception) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(
            "OLLAMA_UNAVAILABLE",
            exception.getMessage(),
            Instant.now()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
}
