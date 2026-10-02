package com.example.B2C.modules.auth.service;

import com.example.B2C.common.email.EmailMessage;
import com.example.B2C.common.email.EmailService;
import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.security.TokenHasher;
import com.example.B2C.modules.auth.entity.PasswordResetToken;
import com.example.B2C.modules.auth.repository.PasswordResetTokenRepository;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenHasher tokenHasher;

    @Value("${application.security.password-reset.token-ttl-minutes:30}")
    private long ttlMinutes;

    @Value("${application.security.password-reset.base-url:http://localhost:3000/reset-password}")
    private String baseUrl;

    public void forgotPassword(String rawEmail) {
        Optional<User> userOpt = userRepository.findByEmail(rawEmail.toLowerCase().trim());
        if (userOpt.isEmpty() || userOpt.get().getDeletedAt() != null) {
            log.info("forgotPassword: email not found or user soft-deleted, returning silently");
            return;
        }

        User user = userOpt.get();

        tokenRepository.deleteByUser(user);
        tokenRepository.flush();

        String rawToken = tokenHasher.generateUrlSafeToken();
        String tokenHash = tokenHasher.hash(rawToken);

        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(ttlMinutes))
                .used(false)
                .build();
        tokenRepository.save(token);

        String link = baseUrl + "?token=" + rawToken;
        String body = "Hello,\n\n"
                + "We received a request to reset your password.\n"
                + "Click the link below to set a new password:\n\n"
                + link + "\n\n"
                + "This link expires in " + ttlMinutes + " minutes.\n"
                + "If you did not request this, you can safely ignore this email.\n";

        emailService.send(new EmailMessage(user.getEmail(), "Reset your password", body));
        log.info("forgotPassword: reset link issued for userId={}", user.getId());
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String hash = tokenHasher.hash(rawToken);
        PasswordResetToken token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (token.isUsed()) {
            throw new BadRequestException("Reset token has already been used");
        }
        if (token.isExpired()) {
            throw new BadRequestException("Reset token has expired");
        }

        User user = token.getUser();
        if (user == null || user.getDeletedAt() != null) {
            throw new BadRequestException("User no longer exists");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        log.info("resetPassword: password updated for userId={}", user.getId());
        // TODO: revoke all refresh tokens for this user (out of scope for this task)
    }
}
