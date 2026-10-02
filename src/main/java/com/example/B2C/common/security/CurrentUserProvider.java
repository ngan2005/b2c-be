package com.example.B2C.common.security;

import com.example.B2C.common.exception.UnauthorizedException;
import com.example.B2C.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserProvider {

    /**
     * Returns the current authenticated user's ID from the security context.
     *
     * @throws UnauthorizedException if no valid authentication principal is found
     */
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new UnauthorizedException("Authentication required");
        }
        return cud.getUser().getId();
    }
}
