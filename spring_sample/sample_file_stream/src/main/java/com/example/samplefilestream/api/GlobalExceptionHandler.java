package com.example.samplefilestream.api;

import com.example.samplefilestream.exception.FileMetadataNotFoundException;
import com.example.samplefilestream.exception.FileStorageOperationException;
import com.example.samplefilestream.exception.StorageBackendUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileMetadataNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
        FileMetadataNotFoundException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse.of("FILE_NOT_FOUND", exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(StorageBackendUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleStorageBackend(
        StorageBackendUnavailableException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiErrorResponse.of("STORAGE_BACKEND_UNAVAILABLE", exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(FileStorageOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleStorageFailure(
        FileStorageOperationException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse.of("FILE_STORAGE_ERROR", exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUpload(
        MaxUploadSizeExceededException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiErrorResponse.of("FILE_TOO_LARGE", "업로드 가능 용량을 초과했습니다.", request.getRequestURI()));
    }
}
