package com.sps.auth.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private String status;
    private T data;
    private String message;
    private String timestamp;

    public ApiResponse(String status, T data, String message, String timestamp) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>("OK", data, message, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", null, message, LocalDateTime.now().toString());
    }

    public String getStatus() { return status; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
}
