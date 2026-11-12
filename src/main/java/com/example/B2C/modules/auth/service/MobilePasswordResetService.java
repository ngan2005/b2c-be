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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobilePasswordResetService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final MobilePasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenHasher tokenHasher;

    @Value("${application.security.password-reset.token-ttl-minutes:5}")
    private long ttlMinutes;

    @Transactional
    public void sendOtp(String rawEmail) {
        String email = rawEmail.toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().getDeletedAt() != null) {
            log.info("mobile sendOtp: email not found or user soft-deleted, returning silently");
            return;
        }

        User user = userOpt.get();

        // Xóa OTP cũ chưa sử dụng của user để dọn dẹp
        otpRepository.deleteByUser(user);
        otpRepository.flush();

        // Sinh mã OTP 6 chữ số ngẫu nhiên
        int number = RANDOM.nextInt(1_000_000);
        String rawOtp = String.format("%06d", number);
        String otpHash = tokenHasher.hash(rawOtp);

        MobilePasswordResetOtp otpRecord = MobilePasswordResetOtp.builder()
                .user(user)
                .otpHash(otpHash)
                .expiresAt(LocalDateTime.now().plusMinutes(ttlMinutes))
                .verified(false)
                .used(false)
                .failedAttempts(0)
                .build();
        otpRepository.save(otpRecord);

        String body = "Xin chào " + (user.getFullName() != null ? user.getFullName() : "") + ",\n\n"
                + "Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản B2C.\n"
                + "Mã xác thực OTP của bạn là:\n\n"
                + "   " + rawOtp + "\n\n"
                + "Mã OTP này có hiệu lực trong " + ttlMinutes + " phút.\n"
                + "Vui lòng không cung cấp mã OTP này cho bất kỳ ai để đảm bảo an toàn tài khoản.\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n";

        emailService.send(new EmailMessage(user.getEmail(), "Mã xác thực OTP đặt lại mật khẩu B2C", body));
        log.info("mobile sendOtp: OTP issued for userId={}", user.getId());
    }

    @Transactional
    public MobileVerifyOtpResponse verifyOtp(String rawEmail, String rawOtp) {
        String email = rawEmail.toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Mã OTP không hợp lệ hoặc tài khoản không tồn tại"));

        if (user.getDeletedAt() != null) {
            throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị khóa");
        }

        MobilePasswordResetOtp otpRecord = otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("Chưa có yêu cầu gửi mã OTP hoặc mã đã được sử dụng"));

        if (otpRecord.isExpired()) {
            throw new BadRequestException("Mã OTP đã hết hiệu lực. Vui lòng nhấn gửi lại mã mới");
        }

        if (otpRecord.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            throw new BadRequestException("Bạn đã nhập sai mã OTP quá 5 lần. Vui lòng yêu cầu mã OTP mới");
        }

        String inputHash = tokenHasher.hash(rawOtp.trim());
        if (!otpRecord.getOtpHash().equals(inputHash)) {
            otpRecord.setFailedAttempts(otpRecord.getFailedAttempts() + 1);
            otpRepository.save(otpRecord);
            int remaining = MAX_FAILED_ATTEMPTS - otpRecord.getFailedAttempts();
            throw new BadRequestException("Mã OTP không chính xác. Bạn còn " + remaining + " lần thử.");
        }

        // Mã OTP đúng -> Cấp resetToken để người dùng chuyển sang màn hình nhập mật khẩu mới
        String resetToken = tokenHasher.generateUrlSafeToken();
        otpRecord.setVerified(true);
        otpRecord.setResetToken(resetToken);
        otpRepository.save(otpRecord);

        log.info("mobile verifyOtp: OTP verified successfully for userId={}", user.getId());
        return MobileVerifyOtpResponse.builder()
                .resetToken(resetToken)
                .build();
    }

    @Transactional
    public void resetPassword(String rawEmail, String resetToken, String newPassword) {
        String email = rawEmail.toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Tài khoản không tồn tại"));

        if (user.getDeletedAt() != null) {
            throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị khóa");
        }

        MobilePasswordResetOtp otpRecord = otpRepository.findByUserAndResetTokenAndUsedFalse(user, resetToken)
                .orElseThrow(() -> new BadRequestException("Yêu cầu đặt lại mật khẩu không hợp lệ hoặc đã hoàn tất"));

        if (!otpRecord.isVerified()) {
            throw new BadRequestException("Mã OTP chưa được xác thực");
        }

        if (otpRecord.isExpired()) {
            throw new BadRequestException("Phiên đặt lại mật khẩu đã hết hạn. Vui lòng thực hiện lại từ đầu");
        }

        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Đánh dấu đã dùng
        otpRecord.setUsed(true);
        otpRepository.save(otpRecord);

        log.info("mobile resetPassword: password updated successfully for userId={}", user.getId());
    }
}
