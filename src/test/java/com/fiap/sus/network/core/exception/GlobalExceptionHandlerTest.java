package com.fiap.sus.network.core.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Map<String, String>> response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().get("error"));
    }

    @Test
    void handleBusinessException_ShouldReturn400() {
        BusinessException ex = new BusinessException("Business error");
        ResponseEntity<Map<String, String>> response = handler.handleBusiness(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Business error", response.getBody().get("error"));
    }

    @Test
    void handleExternalService_ShouldReturn502() {
        ExternalServiceException ex = new ExternalServiceException("External error", new RuntimeException());
        ResponseEntity<Map<String, String>> response = handler.handleExternalService(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("External error", response.getBody().get("error"));
    }
}
