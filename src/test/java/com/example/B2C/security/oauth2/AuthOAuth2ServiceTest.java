package com.example.B2C.security.oauth2;

import com.example.B2C.modules.user.entity.AuthProvider;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CustomOAuth2UserService#processOAuth2User}. We avoid a
 * full {@code @SpringBootTest} and the real Google HTTP fetch by injecting
 * pre-built {@link OAuth2User} instances directly. Each branch of
 * resolveOrCreate is covered: re-login, link-by-email, new user, missing id,
 * missing email.
 */
@ExtendWith(MockitoExtension.class)
class AuthOAuth2ServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks private CustomOAuth2UserService service;

    private Role buyerRole;

    @BeforeEach
    void setUp() {
        buyerRole = Role.builder().id(1L).code("BUYER").name("Buyer").build();
    }

    private OAuth2UserRequest googleRequest() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scope("openid", "email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName("sub")
                .build();
        org.springframework.security.oauth2.core.OAuth2AccessToken token =
                new org.springframework.security.oauth2.core.OAuth2AccessToken(
                        org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
                        "stub-access-token",
                        java.time.Instant.now(),
                        java.time.Instant.now().plusSeconds(3600));
        return new OAuth2UserRequest(registration, token);
    }

    private OAuth2User googleUser(String sub, String email, String name) {
        Map<String, Object> attrs = new HashMap<>();
        if (sub != null) attrs.put("sub", sub);
        if (email != null) attrs.put("email", email);
        if (name != null) attrs.put("name", name);
        return new DefaultOAuth2User(List.of(), attrs, "sub");
    }

    @Test
    @DisplayName("Re-login: same Google sub returns existing social user (no new row)")
    void reLogin_returnsExistingSocialUser() {
        User existing = User.builder()
                .id(7L).email("returning@example.com").fullName("Returning User")
                .provider(AuthProvider.GOOGLE).providerUserId("google-sub-777")
                .providerLinkedAt(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-sub-777"))
                .thenReturn(Optional.of(existing));

        OAuth2User result = service.processOAuth2User(
                googleRequest(), googleUser("google-sub-777", "returning@example.com", "Returning User"));

        assertTrue(result instanceof CustomOAuth2User);
        assertEquals(7L, ((CustomOAuth2User) result).getUser().getId());
        // Did not touch the email path
        verify(userRepository, never()).findByEmail(any());
        // Did not create a new user
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Link-by-email: Google login with email matching local user links the identity")
    void loginWithMatchingEmail_linksExistingLocalUser() {
        User local = User.builder()
                .id(11L).email("member@example.com").fullName("Member")
                .passwordHash("bcrypt-old")
                .provider(AuthProvider.LOCAL).providerUserId(null)
                .status(UserStatus.ACTIVE)
                .role(buyerRole)
                .build();
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-sub-link"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("member@example.com"))
                .thenReturn(Optional.of(local));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        OAuth2User result = service.processOAuth2User(
                googleRequest(), googleUser("google-sub-link", "member@example.com", "Member"));

        assertTrue(result instanceof CustomOAuth2User);
        User returned = ((CustomOAuth2User) result).getUser();
        assertEquals(AuthProvider.GOOGLE, returned.getProvider());
        assertEquals("google-sub-link", returned.getProviderUserId());
        assertNotNull(returned.getProviderLinkedAt());
        // Password was not touched
        assertEquals("bcrypt-old", returned.getPasswordHash());
        // No new user_roles row needed — the user already had BUYER,
        // and the cascade does not re-write existing join rows either.
    }

    @Test
    @DisplayName("First Google login: creates a new BUYER user with provider=GOOGLE")
    void firstLogin_createsNewUser() {
        when(userRepository.findByProviderAndProviderUserId(AuthProvider.GOOGLE, "google-sub-new"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("newbie@example.com"))
                .thenReturn(Optional.empty());
        when(roleRepository.findByCode("BUYER")).thenReturn(Optional.of(buyerRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        OAuth2User result = service.processOAuth2User(
                googleRequest(), googleUser("google-sub-new", "newbie@example.com", "New Bie"));

        assertTrue(result instanceof CustomOAuth2User);
        User returned = ((CustomOAuth2User) result).getUser();
        assertEquals(99L, returned.getId());
        assertEquals(AuthProvider.GOOGLE, returned.getProvider());
        assertEquals("google-sub-new", returned.getProviderUserId());
        assertNull(returned.getPasswordHash());
        // user_roles join row is written by JPA cascade (User.roles @OneToMany +
        // CascadeType.PERSIST + @JoinTable). The service does NOT call
        // userRoleRepository.save() — that would create a duplicate row and
        // violate (user_id, role_id) primary key.
    }

    @Test
    @DisplayName("Provider user id is required: missing sub claim throws OAuth2AuthenticationException")
    void missingProviderUserId_throws() {
        // Use a nameAttributeKey that is present so the DefaultOAuth2User
        // constructor accepts the attributes; the service should still
        // reject because both 'sub' and 'id' are absent.
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("email", "anon@example.com");
        OAuth2User oauth = new DefaultOAuth2User(List.of(), attrs, "email");
        assertThrows(OAuth2AuthenticationException.class,
                () -> service.processOAuth2User(googleRequest(), oauth));
    }

    @Test
    @DisplayName("Email is required: OAuth2 provider without email throws")
    void missingEmail_throws() {
        OAuth2User oauth = googleUser("google-sub-noemail", null, "No Email");
        assertThrows(OAuth2AuthenticationException.class,
                () -> service.processOAuth2User(googleRequest(), oauth));
    }
}
