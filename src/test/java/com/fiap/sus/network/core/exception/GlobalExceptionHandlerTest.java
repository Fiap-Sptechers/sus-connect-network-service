package com.fiap.sus.network.core.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @Test
    void handleResourceNotFound_ShouldReturn404() {
        when(request.getRequestURI()).thenReturn("/test/path");
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ApiError> response = handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not found", response.getBody().message());
        assertEquals("/test/path", response.getBody().path());
    }

    @Test
    void handleBusinessException_ShouldReturn400() {
        when(request.getRequestURI()).thenReturn("/test/path");
        BusinessException ex = new BusinessException("Business error");
        ResponseEntity<ApiError> response = handler.handleBusiness(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Business error", response.getBody().message());
    }

    @Test
    void handleExternalService_ShouldReturn502() {
        when(request.getRequestURI()).thenReturn("/test/path");
        ExternalServiceException ex = new ExternalServiceException("External error", new RuntimeException());
        ResponseEntity<ApiError> response = handler.handleExternalService(ex, request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("External error", response.getBody().message());
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        when(request.getRequestURI()).thenReturn("/test/path");
        Exception ex = new Exception("Generic error");
        ResponseEntity<ApiError> response = handler.handleException(ex, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    @Test
    void handleResourceAlreadyExists_ShouldReturn409() {
        when(request.getRequestURI()).thenReturn("/test/path");
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("Already exists");
        ResponseEntity<ApiError> response = handler.handleResourceAlreadyExists(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Already exists", response.getBody().message());
    }

    @Test
    void handleSecurity_ShouldReturn403() {
        when(request.getRequestURI()).thenReturn("/test/path");
        SecurityException ex = new SecurityException("Security error");
        ResponseEntity<ApiError> response = handler.handleSecurity(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Security error", response.getBody().message());
    }

    @Test
    void handleAccessDenied_ShouldReturn403() {
        when(request.getRequestURI()).thenReturn("/test/path");
        org.springframework.security.access.AccessDeniedException ex = 
            new org.springframework.security.access.AccessDeniedException("Access denied");
        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied", response.getBody().message());
    }
}
