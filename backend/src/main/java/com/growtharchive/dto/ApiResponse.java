package com.growtharchive.dto;

public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    ErrorBody error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, null, new ErrorBody(code, message));
    }

    public record ErrorBody(String code, String message) {
    }
}
