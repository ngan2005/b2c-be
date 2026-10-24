package com.example.B2C.modules.auth.service;

import com.example.B2C.common.email.EmailMessage;
import com.example.B2C.common.email.EmailService;
import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.security.TokenHasher;
import com.example.B2C.modules.auth.entity.PasswordResetToken;
import com.example.B2C.modules.auth.repository.PasswordResetTokenRepository;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    private final TokenHasher tokenHasher = new TokenHasher();

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                userRepository, tokenRepository, passwordEncoder, emailService, tokenHasher);
        ReflectionTestUtils.setField(service, "ttlMinutes", 30L);
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:3000/reset-password");
    }

    private User userWithId(long id, String email, LocalDateTime deletedAt) {
        return User.builder()
                .id(id)
                .email(email)
                .fullName("Test")
                .passwordHash("old-hash")
                .deletedAt(deletedAt)
                .build();
    }

    // ---------------------------------------------------------------------------------------------
    // forgotPassword
    // ---------------------------------------------------------------------------------------------

    @Test
    @DisplayName("forgotPassword with existing active user: creates token, persists hash, sends email")
    void forgotPassword_withExistingEmail_createsTokenAndSendsEmail() {
        User user = userWithId(1L, "user@test.com", null);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.forgotPassword("user@test.com");

        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(any(PasswordResetToken.class));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getTokenHash()).hasSize(64); // SHA-256 hex = 64 chars
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(25));

        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailService).send(emailCaptor.capture());
        EmailMessage email = emailCaptor.getValue();
        assertThat(email.to()).isEqualTo("user@test.com");
        assertThat(email.subject()).isEqualTo("Reset your password");
        // The body must NOT contain the raw token (it does, because the link itself contains it;
        // the raw token is supposed to appear only in the email). The link must include the
        // base URL path.
        assertThat(email.body()).contains("http://localhost:3000/reset-password?token=");
    }

    @Test
    @DisplayName("forgotPassword with non-existing email: no token, no email")
    void forgotPassword_withNonExistingEmail_doesNothing() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        service.forgotPassword("ghost@test.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).send(any());
    }

    @Test
    @DisplayName("forgotPassword with soft-deleted user: no token, no email")
    void forgotPassword_withSoftDeletedUser_doesNotSendEmail() {
        User user = userWithId(1L, "del@test.com", LocalDateTime.now().minusDays(1));
        when(userRepository.findByEmail("del@test.com")).thenReturn(Optional.of(user));

        service.forgotPassword("del@test.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).send(any());
    }

    // ---------------------------------------------------------------------------------------------
    // resetPassword
    // ---------------------------------------------------------------------------------------------

    @Test
    @DisplayName("resetPassword with valid token: encodes new password, marks token used, saves both")
    void resetPassword_withValidToken_updatesPasswordAndMarksUsed() {
        String rawToken = "raw-valid-token-12345";
        String hash = tokenHasher.hash(rawToken);

        User user = userWithId(7L, "user@test.com", null);
        PasswordResetToken token = PasswordResetToken.builder()
                .id(99L)
                .user(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newSecret123")).thenReturn("new-bcrypt-hash");

        service.resetPassword(rawToken, "newSecret123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getId()).isEqualTo(7L);
        assertThat(savedUser.getPasswordHash()).isEqualTo("new-bcrypt-hash");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.isUsed()).isTrue();
        assertThat(savedToken.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("resetPassword with unknown token hash: throws BadRequestException")
    void resetPassword_withUnknownToken_throwsBadRequest() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("does-not-exist", "newSecret123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid or expired reset token");

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword with expired token: throws BadRequestException")
    void resetPassword_withExpiredToken_throwsBadRequest() {
        String rawToken = "raw-expired";
        String hash = tokenHasher.hash(rawToken);

        User user = userWithId(1L, "u@t.com", null);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .used(false)
                .build();

        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(rawToken, "newSecret123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword with already-used token: throws BadRequestException")
    void resetPassword_withAlreadyUsedToken_throwsBadRequest() {
        String rawToken = "raw-used";
        String hash = tokenHasher.hash(rawToken);

        User user = userWithId(1L, "u@t.com", null);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(true)
                .usedAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(rawToken, "newSecret123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been used");

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword when user has been soft-deleted after token issue: throws BadRequestException")
    void resetPassword_withSoftDeletedUser_throwsBadRequest() {
        String rawToken = "raw-deleted-user";
        String hash = tokenHasher.hash(rawToken);

        User user = userWithId(1L, "u@t.com", LocalDateTime.now().minusHours(1));
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.resetPassword(rawToken, "newSecret123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("no longer exists");

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }
}
