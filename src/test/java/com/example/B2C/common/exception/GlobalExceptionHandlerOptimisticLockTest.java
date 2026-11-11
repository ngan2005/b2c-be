package com.example.B2C.common.exception;

import com.example.B2C.common.response.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerOptimisticLockTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("OptimisticLockingFailureException returns 409 Conflict with friendly message")
    void handleSpringOptimisticLockException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/orders");
        var ex = new OptimisticLockingFailureException("Version conflict on row");

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getCode());
        assertEquals("/api/v1/orders", response.getBody().getPath());
        assertTrue(response.getBody().getMessage().contains("Sản phẩm đang có nhiều người cùng đặt mua"));
    }

    @Test
    @DisplayName("Jpa OptimisticLockException returns 409 Conflict with friendly message")
    void handleJpaOptimisticLockException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/orders");
        var ex = new OptimisticLockException("Stale state detected");

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Sản phẩm đang có nhiều người cùng đặt mua"));
    }
}
