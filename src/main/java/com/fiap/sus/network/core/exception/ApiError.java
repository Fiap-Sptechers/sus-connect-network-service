package com.fiap.sus.network.core.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> details
) {
    public ApiError(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }
    
    public ApiError(int status, String error, String message, String path, Map<String, String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}
