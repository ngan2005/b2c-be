package com.example.B2C.modules.user.entity;

/**
 * Identifies the authentication source a {@link User} signed up with. The
 * default {@code LOCAL} represents the legacy email/password path. Social
 * providers are added here as we wire them up (Google first, Facebook later).
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE,
    FACEBOOK
}
