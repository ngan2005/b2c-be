package com.example.B2C.modules.seller.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.seller.dto.AdminSellerDto;
import com.example.B2C.modules.seller.dto.SellerLogoUploadResponse;
import com.example.B2C.modules.seller.dto.SellerProfileResponse;
import com.example.B2C.modules.seller.entity.SellerStatus;
import org.springframework.web.multipart.MultipartFile;

public interface SellerService {

    SellerProfileResponse getMyProfile(Long userId);

    /**
     * Uploads a new shop avatar for the authenticated seller and points
     * {@code seller.logo_url} at it.
     *
     * <p>The previous asset is destroyed from the storage backend so repeated
     * avatar changes do not leak orphaned files.
     *
     * @param userId the authenticated seller's user id
     * @param file   the multipart image (JPEG / PNG / WebP, size-capped by the
     *               storage adapter)
     * @return metadata about the freshly stored asset
     */
    SellerLogoUploadResponse uploadLogo(Long userId, MultipartFile file);

    // ----- Admin-only -----

    PageResponse<AdminSellerDto> listSellersForAdmin(SellerStatus status, String keyword,
                                                      int page, int size);

    AdminSellerDto suspendSellerForAdmin(Long id, String reason);

    AdminSellerDto reactivateSellerForAdmin(Long id);
}
