package com.fiap.sus.network.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception: ", e);
        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            e.getMessage() != null ? e.getMessage() : "An unexpected error occurred",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            details.put(error.getField(), error.getDefaultMessage()));
            
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Invalid request content",
            request.getRequestURI(),
            details
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", e.getMessage());
        String message = e.getMostSpecificCause().getMessage();
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(),
            "Integrity Violation",
            message.contains("duplicate key") ? "Resource already exists" : "Database integrity violation",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON",
            e.getMostSpecificCause().getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Business Rule Violation",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleResourceAlreadyExists(ResourceAlreadyExistsException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiError> handleExternalService(ExternalServiceException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            HttpStatus.BAD_GATEWAY.value(),
            "External Service Error",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleSecurity(SecurityException e, HttpServletRequest request) {
        ApiError error = new ApiError(
            HttpStatus.FORBIDDEN.value(),
            "Security Violation",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
