package com.example.B2C.modules.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for submitting a product review")
public class CreateReviewRequest {

    @NotNull(message = "orderItemId is required")
    @Schema(description = "ID of the order item to review (each order item can only be reviewed once)", example = "42")
    private Long orderItemId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Schema(description = "Star rating from 1 to 5", example = "5")
    private Integer rating;

    @Schema(description = "Review comment text", example = "Great product, fast delivery and excellent quality!")
    private String comment;

    @Schema(description = "URLs of review images (JSON array stored as string in DB)", example = "[\"https://example.com/img1.jpg\"]")
    private String imageUrls;

    @Schema(description = "URL of an optional review video", example = "https://example.com/video1.mp4")
    private String videoUrl;

    @Builder.Default
    @Schema(description = "If true, review is posted anonymously (user name hidden)", example = "false")
    private Boolean isAnonymous = false;
}
