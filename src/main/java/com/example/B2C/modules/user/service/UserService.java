package com.example.B2C.modules.user.service;

import com.example.B2C.modules.user.dto.ChangePasswordRequest;
import com.example.B2C.modules.user.dto.UpdateProfileRequest;
import com.example.B2C.modules.user.dto.UserProfileDto;

public interface UserService {

    UserProfileDto getCurrentUserProfile(Long userId);

    UserProfileDto updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
