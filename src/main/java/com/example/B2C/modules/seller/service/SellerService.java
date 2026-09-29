package com.example.B2C.modules.seller.service;

import com.example.B2C.modules.seller.dto.SellerProfileResponse;

public interface SellerService {

    /**
     * Get the current authenticated seller's profile.
     * Ownership check: only returns the profile belonging to the given userId.
     */
    SellerProfileResponse getMyProfile(Long userId);
}
