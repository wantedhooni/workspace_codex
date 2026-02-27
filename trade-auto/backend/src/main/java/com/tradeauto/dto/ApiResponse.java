package com.tradeauto.dto;

public class ApiResponse<T> {
    public boolean success;
    public T data;
    public String message;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.success = true;
        res.data = data;
        res.message = null;
        return res;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.success = false;
        res.data = null;
        res.message = message;
        return res;
    }
}
