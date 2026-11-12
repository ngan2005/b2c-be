package com.example.B2C.modules.review.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.order.repository.OrderItemRepository;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.review.dto.CreateReviewRequest;
import com.example.B2C.modules.review.dto.ReviewDto;
import com.example.B2C.modules.review.entity.Review;
import com.example.B2C.modules.review.entity.ReviewStatus;
import com.example.B2C.modules.review.repository.ReviewRepository;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    /**
     * Submit a new review for an order item. Only one review per order item.
     */
    @Transactional
    public ReviewDto createReview(Long userId, CreateReviewRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id: " + userId));

        OrderItem orderItem = orderItemRepository.findById(req.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id: " + req.getOrderItemId()));

        if (reviewRepository.existsByOrderItemId(req.getOrderItemId())) {
            throw new BadRequestException("This order item has already been reviewed");
        }

        Product product = productRepository.findByIdAndDeletedAtIsNull(orderItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + orderItem.getProductId()));

        Seller seller = sellerRepository.findById(product.getSeller().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "id: " + product.getSeller().getId()));

        Review review = Review.builder()
                .orderItem(orderItem)
                .product(product)
                .variant(null) // TODO: resolve variant from orderItem if stored
                .user(user)
                .seller(seller)
                .rating(req.getRating())
                .comment(req.getComment())
                .imageUrls(req.getImageUrls())
                .videoUrl(req.getVideoUrl())
                .isAnonymous(Boolean.TRUE.equals(req.getIsAnonymous()))
                .status(ReviewStatus.VISIBLE)
                .build();

        Review saved = reviewRepository.save(review);

        // Mark order item as reviewed
        orderItem.setIsReviewed(true);
        orderItemRepository.save(orderItem);

        // Update product rating
        updateProductRating(product.getId());

        String reviewerName = Boolean.TRUE.equals(review.getIsAnonymous())
                ? "Anonymous" : user.getFullName();
        log.info("Review {} created by user {} for product {}", saved.getId(), userId, product.getId());
        return ReviewDto.from(saved, reviewerName, user.getAvatarUrl());
    }

    /**
     * Get a single review by ID.
     */
    public ReviewDto getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id: " + reviewId));
        String reviewerName = Boolean.TRUE.equals(review.getIsAnonymous())
                ? "Anonymous" : review.getUser().getFullName();
        return ReviewDto.from(review, reviewerName, review.getUser().getAvatarUrl());
    }

    /**
     * List visible reviews for a product (public), optionally filtered by rating.
     */
    public PageResponse<ReviewDto> listByProduct(Long productId, Integer rating, int page, int size) {
        Page<Review> reviews = reviewRepository.findByProductIdAndStatusWithOptionalRating(
                productId, ReviewStatus.VISIBLE, rating,
                PageRequest.of(page, Math.min(size, 100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        return PageResponse.of(reviews.map(r -> {
            String reviewerName = Boolean.TRUE.equals(r.getIsAnonymous())
                    ? "Khách hàng ẩn danh"
                    : (r.getUser() != null && r.getUser().getFullName() != null && !r.getUser().getFullName().isBlank()
                            ? r.getUser().getFullName()
                            : "Khách hàng");
            String avatarUrl = (r.getUser() != null) ? r.getUser().getAvatarUrl() : null;
            return ReviewDto.from(r, reviewerName, avatarUrl);
        }));
    }

    public PageResponse<ReviewDto> listByProduct(Long productId, int page, int size) {
        return listByProduct(productId, null, page, size);
    }

    /**
     * List reviews received by the authenticated seller.
     */
    public PageResponse<ReviewDto> listForCurrentSeller(Long userId, int page, int size) {
        Seller seller = getActiveSeller(userId);
        return listBySellerId(seller.getId(), page, size);
    }

    /**
     * List reviews received by a seller by seller ID for public shop view.
     */
    public PageResponse<ReviewDto> listBySellerId(Long sellerId, int page, int size) {
        Page<Review> reviews = reviewRepository.findBySellerIdAndStatus(
                sellerId, ReviewStatus.VISIBLE, PageRequest.of(page, Math.min(size, 100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        return PageResponse.of(reviews.map(r -> {
            String reviewerName = Boolean.TRUE.equals(r.getIsAnonymous())
                    ? "Anonymous" : (r.getUser() != null ? r.getUser().getFullName() : "Khách hàng");
            String avatarUrl = (r.getUser() != null) ? r.getUser().getAvatarUrl() : null;
            return ReviewDto.from(r, reviewerName, avatarUrl);
        }));
    }

    /**
     * Seller replies to a review on one of their products.
     */
    @Transactional
    public ReviewDto replyToReview(Long userId, Long reviewId, String reply) {
        Seller seller = getActiveSeller(userId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id: " + reviewId));

        if (review.getSeller() == null || !review.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("This review does not belong to your shop");
        }

        review.setSellerReply(reply);
        review.setRepliedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);

        String reviewerName = Boolean.TRUE.equals(saved.getIsAnonymous())
                ? "Anonymous" : saved.getUser().getFullName();
        log.info("Seller {} replied to review {}", userId, reviewId);
        return ReviewDto.from(saved, reviewerName, saved.getUser().getAvatarUrl());
    }

    /**
     * Admin: List all reviews with filters (status, rating, keyword).
     */
    public PageResponse<ReviewDto> listForAdmin(ReviewStatus status, Integer rating, String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            reviews = reviewRepository.findForAdminWithKeyword(status, rating, pattern, pageRequest);
        } else {
            reviews = reviewRepository.findForAdminWithoutKeyword(status, rating, pageRequest);
        }
        return PageResponse.of(reviews.map(r -> {
            String reviewerName = (r.getUser() != null) ? r.getUser().getFullName() : "Người dùng";
            if (Boolean.TRUE.equals(r.getIsAnonymous())) {
                reviewerName += " (Ẩn danh)";
            }
            String avatarUrl = (r.getUser() != null) ? r.getUser().getAvatarUrl() : null;
            return ReviewDto.from(r, reviewerName, avatarUrl);
        }));
    }

    /**
     * Admin: Change review status (e.g. VISIBLE <-> HIDDEN).
     */
    @Transactional
    public ReviewDto updateStatusByAdmin(Long reviewId, ReviewStatus newStatus) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id: " + reviewId));
        review.setStatus(newStatus);
        Review saved = reviewRepository.save(review);
        if (saved.getProduct() != null) {
            updateProductRating(saved.getProduct().getId());
        }
        String reviewerName = (saved.getUser() != null) ? saved.getUser().getFullName() : "Người dùng";
        if (Boolean.TRUE.equals(saved.getIsAnonymous())) {
            reviewerName += " (Ẩn danh)";
        }
        String avatarUrl = (saved.getUser() != null) ? saved.getUser().getAvatarUrl() : null;
        log.info("Admin updated review {} status to {}", reviewId, newStatus);
        return ReviewDto.from(saved, reviewerName, avatarUrl);
    }

    /**
     * Admin: Delete a review.
     */
    @Transactional
    public void deleteByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id: " + reviewId));
        Long productId = (review.getProduct() != null) ? review.getProduct().getId() : null;
        reviewRepository.delete(review);
        if (productId != null) {
            updateProductRating(productId);
        }
        log.info("Admin deleted review {}", reviewId);
    }

    /**
     * Admin: Get review metrics & statistics across platform.
     */
    public com.example.B2C.modules.review.dto.AdminReviewStatsDto getAdminStats() {
        long total = reviewRepository.count();
        long visible = reviewRepository.countByStatus(ReviewStatus.VISIBLE);
        long hidden = reviewRepository.countByStatus(ReviewStatus.HIDDEN);
        Double avgRating = reviewRepository.averagePlatformRating();
        double avg = (avgRating != null) ? Math.round(avgRating * 10.0) / 10.0 : 0.0;

        return com.example.B2C.modules.review.dto.AdminReviewStatsDto.builder()
                .totalReviews(total)
                .visibleCount(visible)
                .hiddenCount(hidden)
                .averageRating(avg)
                .fiveStarCount(reviewRepository.countByRating(5))
                .fourStarCount(reviewRepository.countByRating(4))
                .threeStarCount(reviewRepository.countByRating(3))
                .twoStarCount(reviewRepository.countByRating(2))
                .oneStarCount(reviewRepository.countByRating(1))
                .build();
    }

    private void updateProductRating(Long productId) {
        Double avg = reviewRepository.averageRatingByProductId(productId);
        productRepository.findByIdAndDeletedAtIsNull(productId).ifPresent(p -> {
            p.setRatingAvg(avg != null ? java.math.BigDecimal.valueOf(avg) : java.math.BigDecimal.ZERO);
            p.setRatingCount((int) reviewRepository.findByProductIdAndStatus(
                    productId, ReviewStatus.VISIBLE, PageRequest.of(0, 1)).getTotalElements());
            productRepository.save(p);
        });
    }

    private Seller getActiveSeller(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new ForbiddenException("Seller profile is not active");
        }
        return seller;
    }
}
