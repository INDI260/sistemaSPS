package com.sps.sns.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private String status;
    private T data;
    private String message;
    private String timestamp;

    public ApiResponse() {}

    public ApiResponse(String status, T data, String message, String timestamp) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>("OK", data, message, Instant.now().toString());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", null, message, Instant.now().toString());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
