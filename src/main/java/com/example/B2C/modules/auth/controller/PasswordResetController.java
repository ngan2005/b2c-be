package com.example.B2C.modules.auth.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.auth.dto.ForgotPasswordRequest;
import com.example.B2C.modules.auth.dto.ResetPasswordRequest;
import com.example.B2C.modules.auth.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "Forgot-password flow: send reset link and reset password with token")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request a password reset link",
            description = "Sends a password-reset email to the given address if the account exists. "
                    + "Uses constant-time response to prevent email enumeration."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset email sent (or silent success)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "If the email exists, a reset link has been sent", null));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password using a token",
            description = "Sets a new password using a valid, non-expired reset token. "
                    + "The token is consumed after a successful reset."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid token or password too weak"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Token not found or expired")
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
