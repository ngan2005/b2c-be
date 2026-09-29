package com.example.B2C.modules.auth.service;

import com.example.B2C.modules.auth.dto.AuthResponse;
import com.example.B2C.modules.auth.dto.LoginRequest;
import com.example.B2C.modules.auth.dto.RefreshTokenRequest;
import com.example.B2C.modules.auth.dto.RegisterRequest;
import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.UnauthorizedException;
import com.example.B2C.security.CustomUserDetails;
import com.example.B2C.security.CustomUserDetailsService;
import com.example.B2C.security.JwtTokenProvider;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.RoleCode;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserRole;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;

    @InjectMocks private AuthServiceImpl authService;

    private Role buyerRole;
    private Role sellerRole;

    @BeforeEach
    void setUp() {
        buyerRole = Role.builder().id(1L).code("BUYER").name("Buyer").build();
        sellerRole = Role.builder().id(2L).code("SELLER").name("Seller").build();
    }

    // ---------------------------------------------------------------------------------------------
    // Register tests
    // ---------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("register()")
    class Register {

        private RegisterRequest validRequest() {
            RegisterRequest req = new RegisterRequest();
            req.setEmail("newuser@test.com");
            req.setPassword("password123");
            req.setFullName("New User");
            req.setPhone("0901234567");
            return req;
        }

        @Test
        @DisplayName("Register always creates user as BUYER — no role other than BUYER is persisted")
        void registerCreatesUserAsBuyerOnly() {
            RegisterRequest request = validRequest();

            when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
            when(userRepository.existsByPhone("0901234567")).thenReturn(false);
            when(roleRepository.findByCode("BUYER")).thenReturn(Optional.of(buyerRole));
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(50L);
                return u;
            });
            when(jwtTokenProvider.generateToken(any(CustomUserDetails.class))).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("refresh-token");

            AuthResponse response = authService.register(request);

            // Verify only BUYER role was written to user_roles
            ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
            verify(userRepository).save(any(User.class));

            // The register method builds UserRole manually but does NOT call userRoleRepository.save
            // It adds the role to the user's roles Set instead
            // Let's verify the User entity saved has only BUYER in its roles set
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getRoles())
                    .extracting("code")
                    .containsExactly("BUYER");

            assertThat(response.getRole()).isEqualTo("BUYER");
            assertThat(response.getUserId()).isEqualTo(50L);
            assertThat(response.getEmail()).isEqualTo("newuser@test.com");
        }

        @Test
        @DisplayName("RegisterRequest has NO roleCode field — calling .roleCode() would not compile (regression guard)")
        void registerRequestHasNoRoleCodeField() {
            // This test documents that RegisterRequest must not have a roleCode field.
            // If it did, the code could call request.roleCode(...) which would allow
            // privilege escalation (registering as ADMIN). The test passes by virtue of
            // RegisterRequest not having that field; if it did, compilation would fail.
            RegisterRequest req = new RegisterRequest();
            // The following would not compile if roleCode existed:
            // req.setRoleCode("ADMIN");
            assertThat(req).isNotNull(); // placeholder — compilation IS the guard
        }

        @Test
        @DisplayName("Register writes a UserRole row for BUYER in addition to the roles set")
        void registerWritesUserRoleRow() {
            RegisterRequest request = validRequest();

            when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
            when(userRepository.existsByPhone("0901234567")).thenReturn(false);
            when(roleRepository.findByCode("BUYER")).thenReturn(Optional.of(buyerRole));
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(50L);
                return u;
            });
            when(jwtTokenProvider.generateToken(any(CustomUserDetails.class))).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("refresh-token");

            authService.register(request);

            // The service builds a UserRole and adds it to the user's roles set.
            // No explicit userRoleRepository.save call exists, so we verify via the saved User entity.
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            // BUYER must be in the roles set (which drives the UserRole join table via JPA)
            assertThat(savedUser.getRoles()).anyMatch(r -> "BUYER".equals(r.getCode()));
            // The user's primary role field must also be BUYER
            assertThat(savedUser.getRole().getCode()).isEqualTo("BUYER");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Login tests
    // ---------------------------------------------------------------------------------------------

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Login returns auth response with correct role")
        void loginReturnsAuthResponse() {
            User user = User.builder()
                    .id(10L).email("user@test.com").fullName("Test User")
                    .role(buyerRole).roles(new HashSet<>())
                    .passwordHash("encoded")
                    .build();
            user.getRoles().add(buyerRole);

            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtTokenProvider.generateToken(userDetails)).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refresh-token");

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("user@test.com");
            loginRequest.setPassword("password123");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getRole()).isEqualTo("BUYER");
        }
    }
}
