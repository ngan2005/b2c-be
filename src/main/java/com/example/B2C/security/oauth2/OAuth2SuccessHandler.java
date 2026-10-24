package com.example.B2C.security.oauth2;

import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.util.UserRoleResolver;
import com.example.B2C.security.CustomUserDetails;
import com.example.B2C.security.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Issues a short-lived JWT pair (access + refresh) when an OAuth2 login
 * completes, then 302-redirects the browser to the configured frontend
 * callback URL with the tokens in the URL fragment. Putting tokens in
 * the fragment keeps them out of HTTP server logs (the fragment is not
 * sent to the server).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        User user = null;

        // CustomOAuth2User — our own wrapper carrying the resolved User entity
        // (normal path for Google/Facebook logins without openid scope).
        if (principal instanceof CustomOAuth2User oauth2Principal) {
            user = oauth2Principal.getUser();
        } else if (principal instanceof OidcUser) {
            // DefaultOidcUser — Google login WITH openid scope. Spring Security
            // wraps CustomOAuth2User into OidcUser, losing the User reference.
            // We look it up from the session, where CustomOAuth2UserService stashed it.
            Object fromSession = request.getSession(false) != null
                    ? request.getSession(false).getAttribute(CustomOAuth2UserService.SESSION_ATTR_USER)
                    : null;
            if (fromSession instanceof User resolved) {
                user = resolved;
            } else {
                log.error("OAuth2/OIDC login: no User found in session; "
                        + "principal type={}, sessionId={}",
                        principal.getClass().getName(),
                        request.getSession(false) != null ? request.getSession(false).getId() : "none");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "OAuth2 login could not resolve user; please re-login");
                return;
            }
        } else {
            log.error("OAuth2 success: unexpected principal type {}",
                    principal == null ? "null" : principal.getClass().getName());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "OAuth2 principal type not supported: "
                            + (principal == null ? "null" : principal.getClass().getName()));
            return;
        }

        handleLoginSuccess(request, response, user);

        // Clean up session attribute to avoid retaining a Hibernate-managed entity
        // past the request (the user is now encoded in the JWT).
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute(CustomOAuth2UserService.SESSION_ATTR_USER);
        }
    }

    private void handleLoginSuccess(HttpServletRequest request,
                                     HttpServletResponse response,
                                     User user) throws IOException {
        CustomUserDetails details = new CustomUserDetails(user);
        String accessToken = jwtTokenProvider.generateToken(details);
        String refreshToken = jwtTokenProvider.generateRefreshToken(details);

        // Build: <redirectUri>#access_token=...&refresh_token=...&user_id=...
        StringBuilder fragment = new StringBuilder(redirectUri);
        fragment.append('#');
        fragment.append("access_token=").append(encode(accessToken));
        fragment.append("&refresh_token=").append(encode(refreshToken));
        fragment.append("&user_id=").append(encode(String.valueOf(user.getId())));
        fragment.append("&email=").append(encode(user.getEmail()));
        if (user.getFullName() != null) {
            fragment.append("&full_name=").append(encode(user.getFullName()));
        }
        // Read the highest role from the multi-role join table, not
        // the legacy users.role_id column which some promotion paths
        // forget to update. Without this the frontend receives
        // &role=BUYER for a user who already has SELLER in user_roles,
        // and the navigation guard sends them to the wrong dashboard.
        String roleCode = UserRoleResolver.getHighestRoleCode(user);
        if (roleCode != null) {
            fragment.append("&role=").append(encode(roleCode));
        }

        log.info("OAuth2 login success for user {} ({}); redirecting to {}",
                user.getId(), user.getEmail(), redirectUri);
        getRedirectStrategy().sendRedirect(request, response, fragment.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
