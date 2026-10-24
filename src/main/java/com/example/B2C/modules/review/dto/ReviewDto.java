package com.example.B2C.modules.review.dto;

import com.example.B2C.modules.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Review response payload")
public class ReviewDto {

    @Schema(description = "Review record ID", example = "1")
    private Long id;

    @Schema(description = "ID of the order item this review belongs to", example = "42")
    private Long orderItemId;

    @Schema(description = "Product ID", example = "10")
    private Long productId;

    @Schema(description = "Variant ID (may be null for non-variant products)", example = "15")
    private Long variantId;

    @Schema(description = "Display name of the reviewer (or 'Anonymous' if isAnonymous=true)", example = "Nguyen Van A")
    private String reviewerName;

    @Schema(description = "Reviewer avatar URL", example = "https://example.com/avatar.jpg")
    private String reviewerAvatar;

    @Schema(description = "Seller/shop ID", example = "5")
    private Long sellerId;

    @Schema(description = "Star rating 1–5", example = "5")
    private Integer rating;

    @Schema(description = "Review comment text", example = "Great product, fast delivery!")
    private String comment;

    @Schema(description = "JSON array of image URLs", example = "[\"https://example.com/img1.jpg\"]")
    private String imageUrls;

    @Schema(description = "Optional video URL", example = "https://example.com/video.mp4")
    private String videoUrl;

    @Schema(description = "Whether the reviewer chose to remain anonymous", example = "false")
    private Boolean isAnonymous;

    @Schema(description = "Seller's reply to this review", example = "Thank you for your feedback!")
    private String sellerReply;

    @Schema(description = "When the seller replied", example = "2024-01-15T10:30:00")
    private LocalDateTime repliedAt;

    @Schema(description = "Number of likes/helpful votes", example = "12")
    private Integer likeCount;

    @Schema(description = "Review status: VISIBLE or HIDDEN", example = "VISIBLE")
    private String status;

    @Schema(description = "ISO-8601 creation timestamp", example = "2024-01-10T09:00:00")
    private LocalDateTime createdAt;

    public static ReviewDto from(Review r, String reviewerName, String reviewerAvatar) {
        return ReviewDto.builder()
                .id(r.getId())
                .orderItemId(r.getOrderItem() != null ? r.getOrderItem().getId() : null)
                .productId(r.getProduct() != null ? r.getProduct().getId() : null)
                .variantId(r.getVariant() != null ? r.getVariant().getId() : null)
                .reviewerName(reviewerName)
                .reviewerAvatar(reviewerAvatar)
                .sellerId(r.getSeller() != null ? r.getSeller().getId() : null)
                .rating(r.getRating())
                .comment(r.getComment())
                .imageUrls(r.getImageUrls())
                .videoUrl(r.getVideoUrl())
                .isAnonymous(r.getIsAnonymous())
                .sellerReply(r.getSellerReply())
                .repliedAt(r.getRepliedAt())
                .likeCount(r.getLikeCount())
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
