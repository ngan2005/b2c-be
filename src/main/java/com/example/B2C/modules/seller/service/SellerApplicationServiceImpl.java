package com.example.B2C.modules.seller.service;

import com.example.B2C.common.exception.BusinessException;
import com.example.B2C.common.exception.ErrorCode;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.modules.seller.dto.CreateSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.RejectSellerApplicationRequest;
import com.example.B2C.modules.seller.dto.SellerApplicationAdminDetailResponse;
import com.example.B2C.modules.seller.dto.SellerApplicationResponse;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerApplication;
import com.example.B2C.modules.seller.entity.SellerApplicationStatus;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerApplicationRepository;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.Role;
import com.example.B2C.modules.user.entity.RoleCode;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.entity.UserRole;
import com.example.B2C.modules.user.entity.UserRoleId;
import com.example.B2C.modules.user.repository.RoleRepository;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.modules.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerApplicationServiceImpl implements SellerApplicationService {

    private final SellerApplicationRepository applicationRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    private static final Pattern SLUG_SANITIZE = Pattern.compile("[^a-z0-9\\-]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-+");

    @Override
    @Transactional
    public SellerApplicationResponse submit(Long userId, CreateSellerApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Guard: user must not already be a SELLER.
        // Checked before the BUYER guard because a promoted seller holds BOTH roles, and
        // "already a seller" is the accurate diagnosis - reporting "needs BUYER role" to
        // someone who is an active seller would be actively misleading.
        if (user.hasRole(RoleCode.SELLER.name()) || sellerRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_SELLER);
        }

        // Guard: user must have BUYER role
        if (!user.hasRole(RoleCode.BUYER.name())) {
            throw new BusinessException(ErrorCode.BUYER_ROLE_REQUIRED);
        }

        // Guard: no existing PENDING or APPROVED application
        if (applicationRepository.existsByUserIdAndStatusIn(userId,
                List.of(SellerApplicationStatus.PENDING, SellerApplicationStatus.APPROVED))) {
            throw new BusinessException(ErrorCode.SELLER_APPLICATION_ALREADY_PENDING);
        }

        // Generate unique slug from shop name
        String slug = uniqueSlug(generateSlug(request.getShopName()));

        // Create seller row with PENDING status (required: slug NOT NULL, businessType NOT NULL)
        Seller seller = Seller.builder()
                .user(user)
                .shopName(request.getShopName())
                .slug(slug)
                .businessType(request.getBusinessType())
                .description(request.getShopDescription())
                .status(SellerStatus.PENDING)
                .build();
        sellerRepository.save(seller);

        // Create application
        SellerApplication application = SellerApplication.builder()
                .user(user)
                .shopName(request.getShopName())
                .shopDescription(request.getShopDescription())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(SellerApplicationStatus.PENDING)
                .build();
        SellerApplication saved = applicationRepository.save(application);

        log.info("Seller application submitted: userId={}, applicationId={}", userId, saved.getId());
        return SellerApplicationResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SellerApplicationAdminDetailResponse> list(SellerApplicationStatus status, Pageable pageable) {
        Page<SellerApplication> page;
        if (status != null) {
            page = applicationRepository.findByStatus(status, pageable);
        } else {
            page = applicationRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return page.map(SellerApplicationAdminDetailResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerApplicationAdminDetailResponse getDetail(Long applicationId) {
        SellerApplication app = applicationRepository.findByIdWithUser(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_APPLICATION_NOT_FOUND));
        return SellerApplicationAdminDetailResponse.from(app);
    }

    @Override
    @Transactional
    public SellerApplicationAdminDetailResponse approve(Long adminId, Long applicationId) {
        // Pessimistic lock prevents two admins from approving the same application simultaneously.
        // We hold the lock for the duration of this short transaction, avoiding the lost-update
        // race that optimistic locking (@Version) would surface as a 500 Internal Server Error.
        SellerApplication app = applicationRepository.findByIdWithLock(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_APPLICATION_NOT_FOUND));

        if (app.getStatus() == SellerApplicationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.SELLER_APPLICATION_ALREADY_APPROVED);
        }
        if (app.getStatus() == SellerApplicationStatus.REJECTED) {
            throw new BusinessException(ErrorCode.INVALID_SELLER_APPLICATION_STATUS,
                    "Cannot approve a rejected application");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user", adminId));

        // Add SELLER role only if not already present
        User user = app.getUser();
        if (!user.hasRole(RoleCode.SELLER.name())) {
            Role sellerRole = roleRepository.findByCode(RoleCode.SELLER.name())
                    .orElseThrow(() -> new IllegalStateException("SELLER role not found in database"));
            UserRole userRole = UserRole.builder()
                    .userId(user.getId())
                    .roleId(sellerRole.getId())
                    .build();
            userRoleRepository.save(userRole);
            log.info("Added SELLER role to userId={} on approve applicationId={}", user.getId(), applicationId);
        }

        // Activate seller profile
        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Seller profile not found for user " + user.getId()));
        seller.setStatus(SellerStatus.ACTIVE);
        seller.setApprovedAt(LocalDateTime.now());
        sellerRepository.save(seller);

        // Mark application approved
        app.setStatus(SellerApplicationStatus.APPROVED);
        app.setReviewedBy(admin);
        app.setReviewedAt(LocalDateTime.now());
        SellerApplication saved = applicationRepository.save(app);

        log.info("Seller application approved: applicationId={} by adminId={}", applicationId, adminId);
        return SellerApplicationAdminDetailResponse.from(saved);
    }

    /**
     * The seller row is pre-created at submit time because {@code seller.slug} and
     * {@code seller.business_type} are NOT NULL. If the application is rejected the row
     * has no owner purpose, and leaving it behind would make {@code existsByUserId} block the
     * user from ever re-submitting — so the reservation is released here.
     */
    @Override
    @Transactional
    public SellerApplicationAdminDetailResponse reject(Long adminId, Long applicationId,
            RejectSellerApplicationRequest request) {
        if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw new BusinessException(ErrorCode.REJECTION_REASON_REQUIRED);
        }

        // Pessimistic lock, same as approve: prevents one admin rejecting while another approves.
        SellerApplication app = applicationRepository.findByIdWithLock(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_APPLICATION_NOT_FOUND));

        if (app.getStatus() == SellerApplicationStatus.REJECTED) {
            throw new BusinessException(ErrorCode.SELLER_APPLICATION_ALREADY_REJECTED);
        }
        if (app.getStatus() == SellerApplicationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.INVALID_SELLER_APPLICATION_STATUS,
                    "Cannot reject an approved application");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user", adminId));

        app.setStatus(SellerApplicationStatus.REJECTED);
        app.setRejectionReason(request.getRejectionReason());
        app.setReviewedBy(admin);
        app.setReviewedAt(LocalDateTime.now());
        SellerApplication saved = applicationRepository.save(app);

        // Release the pre-created seller reservation so the applicant can submit again.
        // Only rows still in PENDING are removed; an ACTIVE profile must never be deleted here.
        sellerRepository.findByUserId(app.getUser().getId())
                .filter(seller -> seller.getStatus() == SellerStatus.PENDING)
                .ifPresent(sellerRepository::delete);

        log.info("Seller application rejected: applicationId={} by adminId={}", applicationId, adminId);
        return SellerApplicationAdminDetailResponse.from(saved);
    }

    /**
     * Generates a URL-friendly slug from a shop name.
     * E.g., "My Awesome Shop 123" -> "my-awesome-shop-123"
     */
    private String generateSlug(String shopName) {
        String slug = shopName.toLowerCase().trim();
        slug = SLUG_SANITIZE.matcher(slug).replaceAll("-");
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        // Shop names are frequently non-Latin (e.g. Vietnamese), which sanitises to an
        // empty string; an empty slug would violate the NOT NULL column.
        return slug.isEmpty() ? "shop" : slug;
    }

    /**
     * Appends a short random token instead of probing with existsBySlug: a check-then-act
     * lookup cannot observe a concurrent insert, so two applicants sharing a shop name could
     * both be handed the same candidate and one would fail on the unique constraint.
     */
    private String uniqueSlug(String baseSlug) {
        return baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
