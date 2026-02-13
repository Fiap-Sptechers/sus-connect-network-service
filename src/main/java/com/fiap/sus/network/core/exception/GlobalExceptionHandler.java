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
        String causeObj = e.getCause() != null ? e.getCause().toString() : "N/A";
        log.error("Unhandled exception: {} - Cause: {}", e.getClass().getSimpleName(), e.getMessage());
        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An unexpected error occurred",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getCause());
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
        String message = e.getMostSpecificCause().getMessage();
        log.warn("Data integrity violation: {}", message);
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(),
            "Integrity Violation",
            message != null && message.contains("duplicate key") ? "Resource already exists" : "Database integrity violation",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("Message not readable: {}", e.getMostSpecificCause().getMessage());
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Malformed JSON",
            e.getMostSpecificCause().getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(org.springframework.security.core.AuthenticationException e, HttpServletRequest request) {
        log.warn("Authentication failed: {}", e.getMessage());
        ApiError error = new ApiError(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {}", e.getMessage());
        ApiError error = new ApiError(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler({ResourceNotFoundException.class, jakarta.persistence.EntityNotFoundException.class})
    public ResponseEntity<ApiError> handleResourceNotFound(Exception e, HttpServletRequest request) {
        log.warn("Resource not found: {}", e.getMessage());
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException e, HttpServletRequest request) {
        log.warn("Endpoint not found: {} {}", request.getMethod(), request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            "Endpoint not found: " + request.getRequestURI(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException e, HttpServletRequest request) {
        log.warn("Business rule violation: {}", e.getMessage());
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
        log.warn("Resource already exists: {}", e.getMessage());
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
        log.error("External service error: {}", e.getMessage());
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
        log.warn("Security violation: {}", e.getMessage());
        ApiError error = new ApiError(
            HttpStatus.FORBIDDEN.value(),
            "Security Violation",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
