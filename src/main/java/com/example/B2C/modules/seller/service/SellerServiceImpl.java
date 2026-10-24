package com.example.B2C.modules.seller.service;

import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.storage.StoragePort;
import com.example.B2C.common.storage.StoredAsset;
import com.example.B2C.modules.seller.dto.AdminSellerDto;
import com.example.B2C.modules.seller.dto.SellerLogoUploadResponse;
import com.example.B2C.modules.seller.dto.SellerProfileResponse;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;
    private final StoragePort storagePort;

    @Override
    @Transactional(readOnly = true)
    public SellerProfileResponse getMyProfile(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
        return SellerProfileResponse.from(seller);
    }

    @Override
    @Transactional
    public SellerLogoUploadResponse uploadLogo(Long userId, MultipartFile file) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));

        // Purpose scopes the asset inside the storage bucket so seller
        // avatars stay grouped and are easy to audit.
        String purpose = "sellers/" + seller.getId() + "/logo";
        StoredAsset asset = storagePort.upload(file, purpose);

        // Remember the previous asset so it can be destroyed after the new
        // one is in place. Deleting it before the upload would leave the
        // shop without a logo if the upload failed.
        String previousPublicId = seller.getLogoPublicId();

        seller.setLogoUrl(asset.getSecureUrl());
        seller.setLogoPublicId(asset.getPublicId());
        sellerRepository.save(seller);

        if (previousPublicId != null && !previousPublicId.equals(asset.getPublicId())) {
            try {
                storagePort.delete(previousPublicId);
            } catch (RuntimeException ex) {
                // Never fail the request because cleanup failed — the new
                // avatar is already live. The orphan can be GC'd later.
                log.warn("Could not delete previous seller logo (public_id={}): {}",
                        previousPublicId, ex.getMessage());
            }
        }

        log.info("Seller {} updated shop avatar (public_id={}, {} bytes)",
                seller.getId(), asset.getPublicId(), asset.getBytes());

        return SellerLogoUploadResponse.builder()
                .logoUrl(asset.getSecureUrl())
                .logoPublicId(asset.getPublicId())
                .format(asset.getFormat())
                .bytes(asset.getBytes())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminSellerDto> listSellersForAdmin(SellerStatus status, String keyword,
                                                              int page, int size) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        Page<Seller> sellerPage;
        if (status != null) {
            sellerPage = sellerRepository.findByStatus(status, pageable);
        } else {
            sellerPage = sellerRepository.findAll(pageable);
        }
        return PageResponse.of(sellerPage.map(this::toAdminDto));
    }

    @Override
    @Transactional
    public AdminSellerDto suspendSellerForAdmin(Long id, String reason) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id: " + id));
        if (seller.getStatus() == SellerStatus.SUSPENDED) {
            throw new ConflictException("Seller is already suspended");
        }
        seller.setStatus(SellerStatus.SUSPENDED);
        // Store reason via reflection since suspensionReason field may not exist in entity yet
        try {
            seller.getClass().getMethod("setSuspensionReason", String.class).invoke(seller, reason);
        } catch (NoSuchMethodException e) {
            // Field not yet added to entity — log and continue
            log.warn("Seller entity does not have suspensionReason field yet; reason will not be persisted");
        } catch (Exception e) {
            log.warn("Could not persist suspension reason: {}", e.getMessage());
        }
        Seller saved = sellerRepository.save(seller);
        log.info("Admin suspended seller {} (reason: {})", id, reason);
        return toAdminDto(saved);
    }

    @Override
    @Transactional
    public AdminSellerDto reactivateSellerForAdmin(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id: " + id));
        if (seller.getStatus() != SellerStatus.SUSPENDED) {
            throw new ConflictException("Seller is not suspended");
        }
        seller.setStatus(SellerStatus.ACTIVE);
        try {
            seller.getClass().getMethod("setSuspensionReason", String.class).invoke(seller, (String) null);
        } catch (NoSuchMethodException e) {
            // Field not yet added — ignore
        } catch (Exception e) {
            log.warn("Could not clear suspension reason: {}", e.getMessage());
        }
        Seller saved = sellerRepository.save(seller);
        log.info("Admin reactivated seller {}", id);
        return toAdminDto(saved);
    }

    private AdminSellerDto toAdminDto(Seller seller) {
        return AdminSellerDto.builder()
                .id(seller.getId())
                .userId(seller.getUser() != null ? seller.getUser().getId() : null)
                .userEmail(seller.getUser() != null ? seller.getUser().getEmail() : null)
                .userFullName(seller.getUser() != null ? seller.getUser().getFullName() : null)
                .shopName(seller.getShopName())
                .slug(seller.getSlug())
                .logoUrl(seller.getLogoUrl())
                .description(seller.getDescription())
                .businessType(seller.getBusinessType())
                .taxCode(seller.getTaxCode())
                .ratingAvg(seller.getRatingAvg())
                .ratingCount(seller.getRatingCount())
                .totalProduct(seller.getTotalProduct())
                .status(seller.getStatus())
                .approvedAt(seller.getApprovedAt())
                .createdAt(seller.getCreatedAt())
                .build();
    }
}
