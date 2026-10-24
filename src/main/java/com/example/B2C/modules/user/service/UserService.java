package com.example.B2C.modules.user.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.user.dto.AvatarUploadResponse;
import com.example.B2C.modules.user.dto.ChangePasswordRequest;
import com.example.B2C.modules.user.dto.UpdateProfileRequest;
import com.example.B2C.modules.user.dto.UserProfileDto;
import com.example.B2C.modules.user.entity.UserStatus;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserProfileDto getCurrentUserProfile(Long userId);

    UserProfileDto updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * Uploads a new personal avatar for the authenticated user and points
     * {@code user.avatar_url} at it.
     *
     * <p>The previous asset is destroyed from the storage backend so repeated
     * avatar changes do not leak orphaned files.
     *
     * @param userId the authenticated user's id
     * @param file   the multipart image (JPEG / PNG / WebP, size-capped by the
     *               storage adapter)
     * @return metadata about the freshly stored asset
     */
    AvatarUploadResponse uploadAvatar(Long userId, MultipartFile file);

    void changePassword(Long userId, ChangePasswordRequest request);

    // ----- Admin operations -----

    PageResponse<UserProfileDto> listUsersForAdmin(UserStatus status, String roleCode, String keyword, int page, int size);

    UserProfileDto getUserForAdmin(Long userId);

    UserProfileDto updateUserStatusForAdmin(Long userId, UserStatus status);

    UserProfileDto updateUserRoleForAdmin(Long userId, String roleCode);

    java.util.Set<Long> getOnlineUserIds();
}