package com.example.B2C.modules.user.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.user.dto.ChangePasswordRequest;
import com.example.B2C.modules.user.dto.UpdateProfileRequest;
import com.example.B2C.modules.user.dto.UserProfileDto;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserProfileDto.from(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(user.getPhone())
                    && userRepository.existsByPhone(newPhone)) {
                throw new ConflictException("Phone number already exists: " + newPhone);
            }
            user.setPhone(newPhone);
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for user {}", userId);
        return UserProfileDto.from(saved);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user {}", userId);
    }
}
