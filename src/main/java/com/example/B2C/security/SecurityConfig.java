package com.example.B2C.security;

import com.example.B2C.security.oauth2.CustomOAuth2UserService;
import com.example.B2C.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /**
     * Raw Google client-id as resolved from the environment / .env. When it is
     * still the placeholder string {@code "disabled"} the operator has not
     * configured OAuth2, so we must NOT call {@code .oauth2Login(...)} on the
     * filter chain (Spring Security's auto-config will fail-fast when it tries
     * to wire the authorization endpoint against an empty registration).
     */
    @Value("${spring.security.oauth2.client.registration.google.client-id:disabled}")
    private String googleClientId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean oauth2Enabled = googleClientId != null
                && !googleClientId.isBlank()
                && !"disabled".equalsIgnoreCase(googleClientId.trim());

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                // OAuth2 endpoints — Spring Security handles the redirect dance,
                // the success handler then 302s the browser to the FE callback.
                .requestMatchers(
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/oauth2/authorization/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**", "/api/v1/categories/**", "/api/v1/promotions/**", "/api/v1/promotions", "/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/ai/**").permitAll()
                .requestMatchers("/api/v1/__debug/**").permitAll()
                // SockJS handshake + fallback transport endpoints (xhr-streaming,
                // xhr-polling, etc.) — authentication is checked at STOMP frame
                // time by JwtHandshakeInterceptor + the channel interceptor.
                .requestMatchers("/ws/**").permitAll()
                // SSE endpoints for admin dashboards. EventSource cannot send
                // custom headers, so authentication is checked manually inside
                // the controller using the `?token=` query parameter.
                .requestMatchers("/api/v1/admin/sse/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // ── BUYER-only routes (read-only carve-out below) ──
                .requestMatchers(HttpMethod.POST, "/api/v1/buyer/orders").hasRole("BUYER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/buyer/orders/**").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/v1/buyer/orders/**").hasRole("BUYER")
                .requestMatchers("/api/v1/cart/**").hasRole("BUYER")
                .requestMatchers("/api/v1/wishlist/**").hasRole("BUYER")
                // ── Read-only carve-out: GET /api/v1/buyer/orders/** accessible to BUYER + SELLER ──
                // Allows a SELLER to read historical orders where they were the buyer_id
                // BEFORE they upgraded. POST/PATCH are still locked above.
                .requestMatchers(HttpMethod.GET, "/api/v1/buyer/orders/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/buyer/orders").authenticated()
                // ── SELLER-only routes ──
                .requestMatchers("/api/v1/seller/**").hasRole("SELLER")
                // ── Admin routes ──
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // ── Default: must be authenticated ──
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                // The OAuth2 login itself uses sessions; once the success
                // handler issues a JWT the client switches to stateless auth.
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        if (oauth2Enabled) {
            http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            );
        } else {
            // OAuth2 is intentionally disabled (placeholder client-id). The
            // bean still exists so other components compile, but the filter
            // chain skips wiring the authorization endpoint. FE can detect
            // this by hitting /oauth2/authorization/google (it will return
            // 401) and hide the Google sign-in button.
            org.slf4j.LoggerFactory.getLogger(SecurityConfig.class)
                    .warn("OAuth2 Google sign-in is DISABLED — set GOOGLE_CLIENT_ID "
                            + "and GOOGLE_CLIENT_SECRET in your .env to enable it.");
        }

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
