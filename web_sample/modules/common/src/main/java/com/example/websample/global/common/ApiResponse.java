package com.example.websample.global.common;

/** API 응답 형식을 일관되게 감싸는 공통 응답 모델입니다. */
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        long timestamp

) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, System.currentTimeMillis());
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null, System.currentTimeMillis());
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, null, message, System.currentTimeMillis());
    }
}
