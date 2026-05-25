package com.sps.sam.util;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private String status;
    private T data;
    private String message;
    private String timestamp;

    public static <T> ApiResponse<T> ok(T data, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.status = "OK";
        r.data = data;
        r.message = message;
        r.timestamp = LocalDateTime.now().toString();
        return r;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.status = "ERROR";
        r.data = null;
        r.message = message;
        r.timestamp = LocalDateTime.now().toString();
        return r;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
