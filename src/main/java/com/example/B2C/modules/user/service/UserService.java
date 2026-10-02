package com.example.B2C.modules.user.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.user.dto.ChangePasswordRequest;
import com.example.B2C.modules.user.dto.UpdateProfileRequest;
import com.example.B2C.modules.user.dto.UserProfileDto;
import com.example.B2C.modules.user.entity.UserStatus;

public interface UserService {

    UserProfileDto getCurrentUserProfile(Long userId);

    UserProfileDto updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    // ----- Admin operations -----

    PageResponse<UserProfileDto> listUsersForAdmin(UserStatus status, String roleCode, String keyword, int page, int size);

    UserProfileDto getUserForAdmin(Long userId);

    UserProfileDto updateUserStatusForAdmin(Long userId, UserStatus status);

    UserProfileDto updateUserRoleForAdmin(Long userId, String roleCode);
}