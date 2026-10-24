package com.example.B2C.modules.promotion.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.promotion.dto.*;
import com.example.B2C.modules.promotion.entity.DiscountType;
import com.example.B2C.modules.promotion.entity.Promotion;
import com.example.B2C.modules.promotion.entity.PromotionStatus;
import com.example.B2C.modules.promotion.repository.PromotionRepository;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final SellerRepository sellerRepository;

    /**
     * Create a new promotion for the authenticated seller.
     */
    @Transactional
    public PromotionDto createPromotion(Long userId, CreatePromotionRequest req) {
        Seller seller = getActiveSeller(userId);

        // Normalise the code BEFORE the uniqueness check. The column has a
        // UNIQUE constraint, so checking the raw user input would let
        // "summer26" slip through and then collide with an existing
        // "SUMMER26" row at flush time (surfacing as a 500 constraint
        // violation instead of a clean 409).
        String code = req.getCode().toUpperCase().trim();

        if (promotionRepository.existsByCode(code)) {
            throw new ConflictException("Promotion code already exists: " + code);
        }

        if (req.getEndAt().isBefore(req.getStartAt())) {
            throw new BadRequestException("End time must be after start time");
        }

        if (req.getDiscountType() == DiscountType.PERCENT
                && req.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percentage discount cannot exceed 100");
        }

        // A FREESHIP promotion has no monetary value, but the column is
        // NOT NULL and the DTO requires a non-negative value. Default it to 0
        // so the seller does not have to invent a number for a free-shipping
        // coupon.
        BigDecimal discountValue = req.getDiscountType() == DiscountType.FREESHIP
                ? BigDecimal.ZERO
                : req.getDiscountValue();

        Promotion promotion = Promotion.builder()
                .seller(seller)
                .code(code)
                .name(req.getName().trim())
                .discountType(req.getDiscountType())
                .discountValue(discountValue)
                .maxDiscountAmount(req.getMaxDiscountAmount())
                .minOrderValue(req.getMinOrderValue() != null ? req.getMinOrderValue() : BigDecimal.ZERO)
                .quantity(req.getQuantity())
                .usedCount(0)
                .limitPerUser(req.getLimitPerUser() != null ? req.getLimitPerUser() : 1)
                .applyScope(req.getApplyScope())
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .status(PromotionStatus.ACTIVE)
                .build();

        Promotion saved = promotionRepository.save(promotion);
        log.info("Seller {} created promotion {} (code={})", userId, saved.getId(), saved.getCode());
        return PromotionDto.from(saved);
    }

    /**
     * List all promotions owned by the authenticated seller.
     */
    public PageResponse<PromotionDto> listForCurrentSeller(Long userId, PromotionStatus status, int page, int size) {
        Seller seller = getActiveSeller(userId);
        Page<Promotion> pageResult;
        if (status != null) {
            pageResult = promotionRepository.findBySellerIdAndStatus(
                    seller.getId(), status, PageRequest.of(page, Math.min(size, 100)));
        } else {
            pageResult = promotionRepository.findBySellerId(
                    seller.getId(), PageRequest.of(page, Math.min(size, 100)));
        }
        return PageResponse.of(pageResult.map(PromotionDto::from));
    }

    /**
     * Get a single promotion by ID (seller can only see their own).
     */
    public PromotionDto getPromotion(Long userId, Long promotionId) {
        Seller seller = getActiveSeller(userId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id: " + promotionId));
        if (!seller.getId().equals(promotion.getSeller().getId())) {
            throw new BadRequestException("This promotion does not belong to your shop");
        }
        return PromotionDto.from(promotion);
    }

    /**
     * Disable a promotion (soft-close before expiry).
     */
    @Transactional
    public PromotionDto disablePromotion(Long userId, Long promotionId) {
        Seller seller = getActiveSeller(userId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id: " + promotionId));
        if (!seller.getId().equals(promotion.getSeller().getId())) {
            throw new BadRequestException("This promotion does not belong to your shop");
        }
        promotion.setStatus(PromotionStatus.DISABLED);
        Promotion saved = promotionRepository.save(promotion);
        log.info("Seller {} disabled promotion {}", userId, promotionId);
        return PromotionDto.from(saved);
    }

    /**
     * Update a promotion owned by the authenticated seller.
     *
     * <p>Every editable field is replaced by the incoming payload — this is a
     * full update, not a patch. Fields the platform owns ({@code usedCount},
     * {@code status}, {@code sellerId}) are left untouched so a seller cannot
     * reset redemption counters or un-disable a closed promotion.
     */
    @Transactional
    public PromotionDto updatePromotion(Long userId, Long promotionId, UpdatePromotionRequest req) {
        Seller seller = getActiveSeller(userId);
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id: " + promotionId));
        if (!seller.getId().equals(promotion.getSeller().getId())) {
            throw new BadRequestException("This promotion does not belong to your shop");
        }

        applyUpdate(promotion, req);
        Promotion saved = promotionRepository.save(promotion);
        log.info("Seller {} updated promotion {}", userId, promotionId);
        return PromotionDto.from(saved);
    }

    /**
     * List all active promotions available for the buyer (platform-wide + seller's own).
     */
    public List<PromotionDto> listActivePromotionsForBuyer(Long buyerId) {
        List<Promotion> promotions = promotionRepository.findAllActivePromotions(LocalDateTime.now());
        return promotions.stream().map(PromotionDto::from).toList();
    }

    /**
     * Validate and compute the discount for a promotion code (without redeeming it).
     */
    public ApplyPromotionResponse validatePromotion(Long buyerId, ApplyPromotionRequest req) {
        if (req.getSubtotal() == null) {
            return ApplyPromotionResponse.builder()
                    .code(req.getCode())
                    .success(false)
                    .message("Vui lòng cung cấp giá trị đơn hàng (subtotal)")
                    .build();
        }

        String code = req.getCode() != null ? req.getCode().trim().toUpperCase() : "";
        Promotion promotion = promotionRepository.findByCode(code).orElse(null);

        if (promotion == null) {
            return ApplyPromotionResponse.builder()
                    .code(code)
                    .success(false)
                    .message("Mã giảm giá không tồn tại")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStatus() != PromotionStatus.ACTIVE
                || (promotion.getStartAt() != null && promotion.getStartAt().isAfter(now))
                || (promotion.getEndAt() != null && promotion.getEndAt().isBefore(now))) {
            return ApplyPromotionResponse.builder()
                    .code(code)
                    .success(false)
                    .message("Mã giảm giá đã hết hạn hoặc không còn hiệu lực")
                    .build();
        }

        if (promotion.getQuantity() != null && promotion.getUsedCount() >= promotion.getQuantity()) {
            return ApplyPromotionResponse.builder()
                    .code(code)
                    .success(false)
                    .message("Mã giảm giá đã hết lượt sử dụng")
                    .build();
        }

        if (promotion.getMinOrderValue() != null
                && req.getSubtotal().compareTo(promotion.getMinOrderValue()) < 0) {
            return ApplyPromotionResponse.builder()
                    .code(code)
                    .success(false)
                    .message("Đơn hàng tối thiểu " + formatMoney(promotion.getMinOrderValue()) + " để áp dụng mã này")
                    .build();
        }

        BigDecimal discountAmount = computeDiscount(promotion, req.getSubtotal());

        return ApplyPromotionResponse.builder()
                .code(promotion.getCode())
                .name(promotion.getName())
                .discountType(promotion.getDiscountType().name())
                .discountAmount(discountAmount)
                .remainingBalance(BigDecimal.ZERO)
                .success(true)
                .message("Áp dụng mã giảm giá thành công!")
                .build();
    }

    /**
     * Increment the used count after a successful order (called by order service).
     */
    @Transactional
    public void incrementUsedCount(Long promotionId) {
        promotionRepository.findById(promotionId).ifPresent(p -> {
            if (p.getUsedCount() < p.getQuantity()) {
                p.setUsedCount(p.getUsedCount() + 1);
                promotionRepository.save(p);
            }
        });
    }

    public BigDecimal computeDiscount(Promotion promotion, BigDecimal subtotal) {
        BigDecimal rawDiscount;
        switch (promotion.getDiscountType()) {
            case PERCENT -> {
                rawDiscount = subtotal
                        .multiply(promotion.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            }
            case FIXED -> rawDiscount = promotion.getDiscountValue();
            case FREESHIP -> rawDiscount = BigDecimal.valueOf(30000);
            default -> rawDiscount = BigDecimal.ZERO;
        }

        if (promotion.getMaxDiscountAmount() != null
                && rawDiscount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            rawDiscount = promotion.getMaxDiscountAmount();
        }

        // Discount cannot exceed subtotal (unless freeship)
        if (promotion.getDiscountType() != DiscountType.FREESHIP && rawDiscount.compareTo(subtotal) > 0) {
            rawDiscount = subtotal;
        }

        return rawDiscount.setScale(0, RoundingMode.HALF_UP);
    }

    private String formatMoney(BigDecimal amount) {
        return amount != null ? String.format("%,d₫", amount.longValue()) : "0₫";
    }

    // ----- Admin-only -----

    /**
     * Create a platform-wide promotion (no seller owner).
     *
     * <p>Admin promotions are not tied to a shop, so {@code seller_id} stays
     * NULL. The buyer-facing queries explicitly support this case via
     * {@code (p.seller.id = :sellerId OR p.seller IS NULL)}.
     */
    @Transactional
    public PromotionDto createForAdmin(CreatePromotionRequest req) {
        String code = req.getCode().toUpperCase().trim();

        if (promotionRepository.existsByCode(code)) {
            throw new ConflictException("Promotion code already exists: " + code);
        }

        if (req.getEndAt().isBefore(req.getStartAt())) {
            throw new BadRequestException("End time must be after start time");
        }

        if (req.getDiscountType() == DiscountType.PERCENT
                && req.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percentage discount cannot exceed 100");
        }

        BigDecimal discountValue = req.getDiscountType() == DiscountType.FREESHIP
                ? BigDecimal.ZERO
                : req.getDiscountValue();

        Promotion promotion = Promotion.builder()
                .seller(null)
                .code(code)
                .name(req.getName().trim())
                .discountType(req.getDiscountType())
                .discountValue(discountValue)
                .maxDiscountAmount(req.getMaxDiscountAmount())
                .minOrderValue(req.getMinOrderValue() != null ? req.getMinOrderValue() : BigDecimal.ZERO)
                .quantity(req.getQuantity())
                .usedCount(0)
                .limitPerUser(req.getLimitPerUser() != null ? req.getLimitPerUser() : 1)
                .applyScope(req.getApplyScope())
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .status(PromotionStatus.ACTIVE)
                .build();

        Promotion saved = promotionRepository.save(promotion);
        log.info("Admin created platform promotion {} (code={})", saved.getId(), saved.getCode());
        return PromotionDto.from(saved);
    }

    /**
     * List all promotions across the platform (optionally filtered by status).
     */
    public PageResponse<PromotionDto> listAllForAdmin(PromotionStatus status, int page, int size) {
        Page<Promotion> pageResult;
        if (status != null) {
            pageResult = promotionRepository.findByStatusActive(status,
                    org.springframework.data.domain.PageRequest.of(page, Math.min(size, 100)));
        } else {
            pageResult = promotionRepository.findAllActive(
                    org.springframework.data.domain.PageRequest.of(page, Math.min(size, 100)));
        }
        return PageResponse.of(pageResult.map(PromotionDto::from));
    }

    /**
     * Disable a promotion (admin can disable any promotion regardless of owner).
     */
    @Transactional
    public PromotionDto disableForAdmin(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id: " + promotionId));
        promotion.setStatus(PromotionStatus.DISABLED);
        Promotion saved = promotionRepository.save(promotion);
        log.info("Admin disabled promotion {}", promotionId);
        return PromotionDto.from(saved);
    }

    /**
     * Soft-delete a promotion (admin only). The row is hidden from listings
     * and validation queries but kept so the admin UI can offer a short
     * "undo" action right after deletion. Hard-delete is performed by a
     * separate cleanup job after the undo window expires.
     */
    @Transactional
    public void deleteForAdmin(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id: " + promotionId));
        promotion.setDeletedAt(LocalDateTime.now());
        promotionRepository.save(promotion);
        log.info("Admin soft-deleted promotion {}", promotionId);
    }

    /**
     * Restore a previously soft-deleted promotion within the undo window.
     */
    @Transactional
    public PromotionDto restoreForAdmin(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .filter(p -> p.getDeletedAt() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id: " + promotionId));
        promotion.setDeletedAt(null);
        Promotion saved = promotionRepository.save(promotion);
        log.info("Admin restored promotion {}", promotionId);
        return PromotionDto.from(saved);
    }

    /**
     * Applies an update payload onto an existing promotion.
     *
     * <p>Shared by the seller and admin update paths so both enforce exactly
     * the same rules. The caller is responsible for having already verified
     * ownership and code uniqueness.
     */
    private void applyUpdate(Promotion promotion, UpdatePromotionRequest req) {
        String code = req.getCode().toUpperCase().trim();

        // Ignore the row's own code — keeping the same code is a no-op, not a
        // conflict.
        if (promotionRepository.existsByCodeAndIdNot(code, promotion.getId())) {
            throw new ConflictException("Promotion code already exists: " + code);
        }

        if (req.getEndAt().isBefore(req.getStartAt())) {
            throw new BadRequestException("End time must be after start time");
        }

        if (req.getDiscountType() == DiscountType.PERCENT
                && req.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Percentage discount cannot exceed 100");
        }

        // The total quota cannot drop below what has already been redeemed —
        // otherwise remainingQuantity goes negative and the promotion could
        // still be redeemed past its limit.
        if (req.getQuantity() < promotion.getUsedCount()) {
            throw new BadRequestException(
                    "Total quantity cannot be less than the number of times already used ("
                            + promotion.getUsedCount() + ")");
        }

        BigDecimal discountValue = req.getDiscountType() == DiscountType.FREESHIP
                ? BigDecimal.ZERO
                : req.getDiscountValue();

        promotion.setCode(code);
        promotion.setName(req.getName().trim());
        promotion.setDiscountType(req.getDiscountType());
        promotion.setDiscountValue(discountValue);
        promotion.setMaxDiscountAmount(req.getMaxDiscountAmount());
        promotion.setMinOrderValue(req.getMinOrderValue() != null ? req.getMinOrderValue() : BigDecimal.ZERO);
        promotion.setQuantity(req.getQuantity());
        promotion.setLimitPerUser(req.getLimitPerUser() != null ? req.getLimitPerUser() : 1);
        promotion.setApplyScope(req.getApplyScope());
        promotion.setStartAt(req.getStartAt());
        promotion.setEndAt(req.getEndAt());
    }

    /**
     * Update any promotion (admin can edit seller-owned promotions too).
     */
    @Transactional
    public PromotionDto updateForAdmin(Long promotionId, UpdatePromotionRequest req) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id: " + promotionId));

        applyUpdate(promotion, req);
        Promotion saved = promotionRepository.save(promotion);
        log.info("Admin updated promotion {}", promotionId);
        return PromotionDto.from(saved);
    }

    private Seller getActiveSeller(Long userId) {
        return sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
    }
}
