package com.example.B2C.common.exception;

import com.example.B2C.common.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerAuthenticationTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BadCredentialsException with custom message preserves message")
    void handleBadCredentials_customMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        var ex = new BadCredentialsException("Mật khẩu không chính xác. Vui lòng kiểm tra lại");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getCode());
        assertEquals("Mật khẩu không chính xác. Vui lòng kiểm tra lại", response.getBody().getMessage());
    }

    @Test
    @DisplayName("BadCredentialsException with default message falls back to friendly password message")
    void handleBadCredentials_defaultMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        var ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Mật khẩu không chính xác. Vui lòng kiểm tra lại", response.getBody().getMessage());
    }

    @Test
    @DisplayName("LockedException returns 401 with friendly locked message")
    void handleLockedException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        var ex = new LockedException("User account is locked");

        ResponseEntity<ErrorResponse> response = handler.handleLocked(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Tài khoản đã bị khóa"));
    }

    @Test
    @DisplayName("DisabledException returns 401 with friendly disabled message")
    void handleDisabledException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        var ex = new DisabledException("User is disabled");

        ResponseEntity<ErrorResponse> response = handler.handleDisabled(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Tài khoản chưa được kích hoạt"));
    }

    @Test
    @DisplayName("AccountExpiredException returns 401 with friendly expired message")
    void handleAccountExpiredException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        var ex = new AccountExpiredException("User account has expired");

        ResponseEntity<ErrorResponse> response = handler.handleAccountExpired(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Tài khoản đã hết hạn"));
    }

    @Test
    @DisplayName("UsernameNotFoundException returns 401 with friendly email not found message")
    void handleUsernameNotFoundException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        var ex = new UsernameNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response = handler.handleUsernameNotFound(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Email không tồn tại"));
    }
}

