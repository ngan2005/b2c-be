package com.example.B2C.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "JWT authentication response returned after login or registration")
public class AuthResponse {

    @Schema(description = "JWT access token (short-lived, typically 15–60 minutes)",
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.sig")
    private String accessToken;

    @Schema(description = "JWT refresh token (long-lived, used to obtain new access tokens)",
            example = "eyJhbGciOiJIUzI1NiJ9.refresh.sig")
    private String refreshToken;

    @Schema(description = "Token type — always 'Bearer'", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Authenticated user's database ID", example = "1")
    private Long userId;

    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Schema(description = "User full display name", example = "Nguyen Van A")
    private String fullName;

    /**
     * Primary (highest-priority) role: BUYER, SELLER, or ADMIN.
     * Kept for backward compatibility with legacy frontend code that reads
     * {@code user.role} as a single string.
     */
    @Schema(description = "Primary role: BUYER, SELLER, or ADMIN", example = "BUYER")
    private String role;

    /**
     * All roles the user holds, ordered by priority descending
     * (ADMIN &gt; SELLER &gt; BUYER). Use this for multi-role UI scenarios
     * such as showing a BUYER+SELLER switcher or rendering both sets of nav items.
     */
    @Schema(description = "All roles the user holds", example = "[\"BUYER\",\"SELLER\"]")
    private List<String> roles;
}
