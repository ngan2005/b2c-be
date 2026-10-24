package com.example.B2C.modules.seller.service;

import com.example.B2C.modules.seller.dto.CreateSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.QuickUpgradeRequest;
import com.example.B2C.modules.seller.dto.QuickUpgradeResponse;
import com.example.B2C.modules.seller.dto.RejectSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.SellerApplicationAdminDetailResponse;
import com.example.B2C.modules.seller.dto.SellerApplicationResponse;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// [SELLER-BUYER-UPGRADE]
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

    // [SELLER-BUYER-UPGRADE]
    /**
     * Quick-upgrade a BUYER to SELLER without admin review.
     * <p>
     * Adds the SELLER role to the user's existing account, creates an ACTIVE seller
     * profile, and returns fresh JWT tokens so the frontend can update its auth store
     * without forcing a re-login.
     *
     * @param userId the current BUYER's database ID
     * @param req    shop details for the new seller profile
     * @return a response carrying the new seller ID, shop name, status, and fresh tokens
     * @throws com.example.B2C.common.exception.BusinessException with
     *         {@code USER_ALREADY_SELLER} if the user already has a seller profile,
     *         {@code BUYER_ROLE_REQUIRED} if the user lacks the BUYER role
     */
    QuickUpgradeResponse quickUpgrade(Long userId, QuickUpgradeRequest req);
}
