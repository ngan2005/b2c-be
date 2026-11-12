package com.example.B2C.modules.auth.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.auth.dto.AuthResponse;
import com.example.B2C.modules.auth.dto.GoogleLoginRequest;
import com.example.B2C.modules.auth.dto.LoginRequest;
import com.example.B2C.modules.auth.dto.RefreshTokenRequest;
import com.example.B2C.modules.auth.dto.RegisterRequest;
import com.example.B2C.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, and JWT token refresh")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user account",
            description = "Creates a new user with the BUYER role. "
                    + "Password must be at least 8 characters. "
                    + "Returns JWT access and refresh tokens on success."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created and tokens returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed or email already registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Unprocessable entity")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate and obtain JWT tokens",
            description = "Validates credentials and returns a short-lived JWT access token "
                    + "and a long-lived refresh token. Include the access token as "
                    + "`Authorization: Bearer <token>` in subsequent requests."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/google")
    @Operation(
            summary = "Authenticate with Google ID Token (Mobile / SPA)",
            description = "Validates the Google ID token obtained from native Google Sign-In, "
                    + "links or auto-registers the user, and returns JWT access and refresh tokens."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired Google token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or malformed ID token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập Google thành công", response));
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh an expired access token",
            description = "Accepts a valid refresh token and returns a new access token + refresh token pair. "
                    + "The old refresh token is invalidated after use (rotation)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "New token pair returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token missing, expired, or revoked")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Log out user (P2.2)",
            description = "Stateless logout — client discards stored access and refresh tokens."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out successfully")
    })
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
