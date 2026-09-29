package com.example.B2C.modules.seller.service;

import com.example.B2C.modules.seller.dto.CreateSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.RejectSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.SellerApplicationAdminDetailResponse;
import com.example.B2C.modules.seller.dto.SellerApplicationResponse;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerApplicationService {

    /**
     * Submit a new seller application.
     * The user must have BUYER role and not already be a SELLER.
     * A PENDING seller row is created immediately (required for slug/business_type constraints).
     */
    SellerApplicationResponse submit(Long userId, CreateSellerApplicationRequest request);

    /**
     * List all applications (admin view), optionally filtered by status.
     */
    Page<SellerApplicationAdminDetailResponse> list(SellerApplicationStatus status, Pageable pageable);

    /**
     * Get admin detail of a single application.
     */
    SellerApplicationAdminDetailResponse getDetail(Long applicationId);

    /**
     * Approve an application (admin action).
     * Adds SELLER role, activates seller profile, marks application APPROVED.
     * Uses pessimistic write lock to prevent race conditions between admins.
     */
    SellerApplicationAdminDetailResponse approve(Long adminId, Long applicationId);

    /**
     * Reject an application (admin action).
     * Requires a non-blank rejection reason.
     */
    SellerApplicationAdminDetailResponse reject(Long adminId, Long applicationId, RejectSellerApplicationRequest request);
}
