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
import com.example.B2C.modules.user.repository.UserRoleRepository;
import com.example.B2C.modules.user.util.UserRoleResolver;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone number already exists: " + request.getPhone());
        }

        // Always create new users as BUYER
        Role buyerRole = roleRepository.findByCode(RoleCode.BUYER.name())
                .orElseThrow(() -> new IllegalStateException("BUYER role not found in database"));

        User newUser = User.builder()
                .role(buyerRole)
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(Set.of(buyerRole)))
                .build();

        User savedUser = userRepository.save(newUser);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);

        Map<String, Object> claims = Map.of("uid", savedUser.getId());
        String accessToken = jwtTokenProvider.generateToken(claims, userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(RoleCode.BUYER.name())
                .roles(List.of(RoleCode.BUYER.name()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            boolean emailExists = userRepository.existsByEmail(request.getEmail());
            if (!emailExists) {
                throw new BadCredentialsException("Email không tồn tại trong hệ thống");
            } else {
                throw new BadCredentialsException("Mật khẩu không chính xác. Vui lòng kiểm tra lại");
            }
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Map<String, Object> claims = Map.of("uid", user.getId());
        String accessToken = jwtTokenProvider.generateToken(claims, userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(UserRoleResolver.getHighestRoleCode(user))
                .roles(UserRoleResolver.getAllRoleCodes(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String username = jwtTokenProvider.extractUsername(token);

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        if (!jwtTokenProvider.isTokenValid(token, userDetails)) {
            throw new UnauthorizedException("Invalid or expired Refresh Token");
        }

        User user = userDetails.getUser();
        Map<String, Object> claims = Map.of("uid", user.getId());
        String newAccessToken = jwtTokenProvider.generateToken(claims, userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(UserRoleResolver.getHighestRoleCode(user))
                .roles(UserRoleResolver.getAllRoleCodes(user))
                .build();
    }
}
