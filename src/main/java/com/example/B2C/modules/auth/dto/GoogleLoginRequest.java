package com.example.B2C.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {

    @NotBlank(message = "idToken không được để trống")
    @Schema(description = "Google ID Token nhận được từ Google Sign-In SDK (Android/iOS/Web)", example = "eyJhbGciOiJSUzI1NiIsImtpZCI...")
    private String idToken;
}

