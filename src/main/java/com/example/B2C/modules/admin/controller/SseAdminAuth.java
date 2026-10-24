package com.example.B2C.modules.admin.controller;

import com.example.B2C.security.CustomUserDetailsService;
import com.example.B2C.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Reusable helper for the SSE controllers: validates the {@code ?token=}
 * query parameter and ensures the subject user holds {@code ROLE_ADMIN}.
 *
 * <p>The DB lookup gives us live role data — a token from a user whose
 * role was downgraded to BUYER will be rejected immediately. Tokens without
 * a role claim (which is the case for our issuer) still work because we
 * load the authorities from {@link CustomUserDetailsService}.
 */
@Component
@RequiredArgsConstructor
public class SseAdminAuth {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public void requireAdmin(String token) {
        if (token == null || token.isBlank()) {
            throw new AccessDeniedException("Missing token");
        }
        try {
            Claims claims = jwtTokenProvider.extractClaim(token, c -> c);
            String username = claims.getSubject();
            if (username == null || username.isBlank()) {
                throw new AccessDeniedException("Invalid token subject");
            }
            if (claims.getExpiration() == null
                    || claims.getExpiration().before(new Date())) {
                throw new AccessDeniedException("Token expired");
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtTokenProvider.isTokenValid(token, userDetails)) {
                throw new AccessDeniedException("Token invalid");
            }
            boolean isAdmin = false;
            for (GrantedAuthority a : userDetails.getAuthorities()) {
                if ("ROLE_ADMIN".equals(a.getAuthority())) {
                    isAdmin = true;
                    break;
                }
            }
            if (!isAdmin) {
                throw new AccessDeniedException("Admin role required");
            }
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AccessDeniedException("Invalid token: " + ex.getMessage());
        }
    }
}
