package com.bloodbank.common.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private int status; // HTTP status code (e.g., 200, 400, 404, 500)
    private String message;
    private T data;
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private String traceId;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message("Operation completed successfully")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(int status, String message, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .traceId(traceId)
                .build();
    }

    public static <T> ApiResponse<T> failure(int status, String message, T data, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .data(data)
                .traceId(traceId)
                .build();
    }
}
