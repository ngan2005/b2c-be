package com.example.B2C.modules.user.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.storage.StoragePort;
import com.example.B2C.common.storage.StoredAsset;
import com.example.B2C.modules.user.dto.AvatarUploadResponse;
import com.example.B2C.modules.user.dto.ChangePasswordRequest;
import com.example.B2C.modules.user.dto.UpdateProfileRequest;
import com.example.B2C.modules.user.dto.UserProfileDto;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserStatus;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StoragePort storagePort;
    private final com.example.B2C.realtime.UserPresenceService userPresenceService;

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
    public AvatarUploadResponse uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Purpose scopes the asset inside the storage bucket so user avatars
        // stay grouped and are easy to audit.
        String purpose = "users/" + userId + "/avatar";
        StoredAsset asset = storagePort.upload(file, purpose);

        // Remember the previous asset so it can be destroyed after the new one
        // is in place. Deleting it before the upload would leave the user
        // without an avatar if the upload failed.
        String previousPublicId = user.getAvatarPublicId();

        user.setAvatarUrl(asset.getSecureUrl());
        user.setAvatarPublicId(asset.getPublicId());
        userRepository.save(user);

        if (previousPublicId != null && !previousPublicId.equals(asset.getPublicId())) {
            try {
                storagePort.delete(previousPublicId);
            } catch (RuntimeException ex) {
                // Never fail the request because cleanup failed — the new
                // avatar is already live. The orphan can be GC'd later.
                log.warn("Could not delete previous user avatar (public_id={}): {}",
                        previousPublicId, ex.getMessage());
            }
        }

        log.info("User {} updated avatar (public_id={}, {} bytes)",
                userId, asset.getPublicId(), asset.getBytes());

        return AvatarUploadResponse.builder()
                .avatarUrl(asset.getSecureUrl())
                .avatarPublicId(asset.getPublicId())
                .format(asset.getFormat())
                .bytes(asset.getBytes())
                .build();
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

    // ===================== Admin operations =====================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserProfileDto> listUsersForAdmin(UserStatus status, String roleCode, String keyword,
                                                          int page, int size) {
        String statusStr = (status == null) ? null : status.name();
        String trimmed = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String role = (roleCode == null || roleCode.isBlank()) ? null : roleCode.trim().toUpperCase();
        int p = Math.max(page, 0);
        int s = clampSize(size);
        Pageable pageable = PageRequest.of(p, s);
        Page<User> users = userRepository.searchUsers(statusStr, role, trimmed, pageable);
        return PageResponse.of(users.map(u -> {
            UserProfileDto dto = UserProfileDto.from(u);
            dto.setIsOnline(userPresenceService.isUserOnline(u.getId()));
            return dto;
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserForAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        UserProfileDto dto = UserProfileDto.from(user);
        dto.setIsOnline(userPresenceService.isUserOnline(user.getId()));
        return dto;
    }

    @Override
    public Set<Long> getOnlineUserIds() {
        return userPresenceService.getOnlineUserIds();
    }

    @Override
    @Transactional
    public UserProfileDto updateUserStatusForAdmin(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setStatus(status);
        User saved = userRepository.save(user);
        log.info("Admin changed status of user {} to {}", userId, status);
        return UserProfileDto.from(saved);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserRoleForAdmin(Long userId, String roleCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Role role = roleRepository.findByCode(roleCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code: " + roleCode));

        // Update both legacy single-role column and multi-role collection so JWT
        // authorities pick up the change on next login (token refresh required).
        user.setRole(role);
        Set<Role> updated = new HashSet<>();
        updated.add(role);
        user.setRoles(updated);

        User saved = userRepository.save(user);
        log.info("Admin changed role of user {} to {}", userId, role.getCode());
        return UserProfileDto.from(saved);
    }

    private int clampSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, 100);
    }
}