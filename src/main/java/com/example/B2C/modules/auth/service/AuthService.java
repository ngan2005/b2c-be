package com.example.B2C.modules.auth.service;

import com.example.B2C.modules.auth.dto.AuthResponse;
import com.example.B2C.modules.auth.dto.LoginRequest;
import com.example.B2C.modules.auth.dto.RefreshTokenRequest;
import com.example.B2C.modules.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}
