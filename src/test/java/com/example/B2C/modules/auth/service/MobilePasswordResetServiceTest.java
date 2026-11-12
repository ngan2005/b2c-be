package com.example.B2C.modules.auth.service;

import com.example.B2C.common.email.EmailMessage;
import com.example.B2C.common.email.EmailService;
import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.security.TokenHasher;
import com.example.B2C.modules.auth.dto.MobileVerifyOtpResponse;
import com.example.B2C.modules.auth.entity.MobilePasswordResetOtp;
import com.example.B2C.modules.auth.repository.MobilePasswordResetOtpRepository;
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
class MobilePasswordResetServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private MobilePasswordResetOtpRepository otpRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    private final TokenHasher tokenHasher = new TokenHasher();

    private MobilePasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new MobilePasswordResetService(
                userRepository, otpRepository, passwordEncoder, emailService, tokenHasher);
        ReflectionTestUtils.setField(service, "ttlMinutes", 5L);
    }

    private User userWithId(long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .fullName("Mobile Test User")
                .passwordHash("old-password-hash")
                .build();
    }

    @Test
    @DisplayName("sendOtp: generates 6-digit OTP, hashes it, saves record, and sends email")
    void sendOtp_success() {
        User user = userWithId(10L, "mobile@example.com");
        when(userRepository.findByEmail("mobile@example.com")).thenReturn(Optional.of(user));

        service.sendOtp("mobile@example.com");

        verify(otpRepository).deleteByUser(user);
        ArgumentCaptor<MobilePasswordResetOtp> otpCaptor = ArgumentCaptor.forClass(MobilePasswordResetOtp.class);
        verify(otpRepository).save(otpCaptor.capture());
        MobilePasswordResetOtp saved = otpCaptor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getOtpHash()).isNotBlank();
        assertThat(saved.isVerified()).isFalse();
        assertThat(saved.isUsed()).isFalse();

        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailService).send(emailCaptor.capture());
        EmailMessage sent = emailCaptor.getValue();
        assertThat(sent.to()).isEqualTo("mobile@example.com");
        assertThat(sent.subject()).contains("OTP");
    }

    @Test
    @DisplayName("verifyOtp: valid OTP returns resetToken and marks verified")
    void verifyOtp_success() {
        User user = userWithId(10L, "mobile@example.com");
        String rawOtp = "123456";
        String otpHash = tokenHasher.hash(rawOtp);

        MobilePasswordResetOtp record = MobilePasswordResetOtp.builder()
                .id(1L)
                .user(user)
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(4))
                .verified(false)
                .used(false)
                .failedAttempts(0)
                .build();

        when(userRepository.findByEmail("mobile@example.com")).thenReturn(Optional.of(user));
        when(otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)).thenReturn(Optional.of(record));

        MobileVerifyOtpResponse response = service.verifyOtp("mobile@example.com", "123456");

        assertThat(response.getResetToken()).isNotBlank();
        assertThat(record.isVerified()).isTrue();
        assertThat(record.getResetToken()).isEqualTo(response.getResetToken());
        verify(otpRepository).save(record);
    }

    @Test
    @DisplayName("verifyOtp: wrong OTP increments failed attempts and throws")
    void verifyOtp_wrongOtp_throws() {
        User user = userWithId(10L, "mobile@example.com");
        String otpHash = tokenHasher.hash("123456");

        MobilePasswordResetOtp record = MobilePasswordResetOtp.builder()
                .id(1L)
                .user(user)
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(4))
                .verified(false)
                .used(false)
                .failedAttempts(0)
                .build();

        when(userRepository.findByEmail("mobile@example.com")).thenReturn(Optional.of(user));
        when(otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.verifyOtp("mobile@example.com", "999999"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mã OTP không chính xác");

        assertThat(record.getFailedAttempts()).isEqualTo(1);
        verify(otpRepository).save(record);
    }

    @Test
    @DisplayName("resetPassword: valid token updates password and marks used")
    void resetPassword_success() {
        User user = userWithId(10L, "mobile@example.com");
        String resetToken = "valid-reset-token";

        MobilePasswordResetOtp record = MobilePasswordResetOtp.builder()
                .id(1L)
                .user(user)
                .resetToken(resetToken)
                .expiresAt(LocalDateTime.now().plusMinutes(4))
                .verified(true)
                .used(false)
                .build();

        when(userRepository.findByEmail("mobile@example.com")).thenReturn(Optional.of(user));
        when(otpRepository.findByUserAndResetTokenAndUsedFalse(user, resetToken)).thenReturn(Optional.of(record));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-password");

        service.resetPassword("mobile@example.com", resetToken, "newPassword123");

        assertThat(user.getPasswordHash()).isEqualTo("new-encoded-password");
        assertThat(record.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(otpRepository).save(record);
    }
}
