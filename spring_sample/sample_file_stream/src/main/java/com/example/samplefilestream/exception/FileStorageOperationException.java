package com.example.samplefilestream.exception;

public class FileStorageOperationException extends RuntimeException {

    public FileStorageOperationException(String message) {
        super(message);
    }

    public FileStorageOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
