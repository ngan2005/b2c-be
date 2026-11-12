package com.example.B2C.modules.auth.repository;

import com.example.B2C.modules.auth.entity.MobilePasswordResetOtp;
import com.example.B2C.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobilePasswordResetOtpRepository extends JpaRepository<MobilePasswordResetOtp, Long> {

    Optional<MobilePasswordResetOtp> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);

    Optional<MobilePasswordResetOtp> findByUserAndResetTokenAndUsedFalse(User user, String resetToken);

    void deleteByUser(User user);
}
