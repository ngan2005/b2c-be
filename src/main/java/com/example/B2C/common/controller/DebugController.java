package com.example.B2C.common.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Debug-only endpoints to help generate BCrypt hashes for fixing seed data
 * without leaving the IDE. NOT exposed in production.
 */
@RestController
@RequestMapping("/api/v1/__debug")
public class DebugController {

    private final PasswordEncoder passwordEncoder;

    public DebugController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/bcrypt")
    public Map<String, String> bcrypt(@RequestParam(defaultValue = "password123") String password) {
        String hash = passwordEncoder.encode(password);
        return Map.of(
                "password", password,
                "hash", hash,
                "verify", String.valueOf(passwordEncoder.matches(password, hash))
        );
    }
}
