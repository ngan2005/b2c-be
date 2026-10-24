package com.example.B2C.modules.review.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.review.dto.CreateReviewRequest;
import com.example.B2C.modules.review.dto.ReviewDto;
import com.example.B2C.modules.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reviews", description = "Product review and rating APIs")
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/buyer/reviews")
    @PreAuthorize("hasRole('BUYER') or hasRole('ADMIN')")
    @Operation(
            summary = "Submit a product review",
            description = "Authenticated buyer submits a review for an order item they have purchased. "
                    + "Each order item can only be reviewed once. The order item is automatically marked as reviewed."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or order item already reviewed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order item or product not found")
    })
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(@Valid @RequestBody CreateReviewRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        ReviewDto dto = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
    }

    @GetMapping("/products/{productId}/reviews")
    @Operation(
            summary = "List visible reviews for a product",
            description = "Public endpoint — returns paginated, visible reviews for a given product. "
                    + "Anonymous reviewers show as 'Anonymous'."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<ReviewDto>>> listByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.listByProduct(productId, page, size)));
    }

    @GetMapping("/seller/reviews")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(
            summary = "List reviews received by the authenticated seller",
            description = "Returns all visible reviews on the seller's products, ordered by newest first."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not a seller")
    })
    public ResponseEntity<ApiResponse<PageResponse<ReviewDto>>> listForSeller(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.listForCurrentSeller(userId, page, size)));
    }

    @PostMapping("/seller/reviews/{reviewId}/reply")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(
            summary = "Reply to a product review",
            description = "The authenticated seller replies to a review on one of their products. "
                    + "Only one reply is allowed per review; subsequent calls overwrite the previous reply."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reply saved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Review does not belong to this seller"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponse<ReviewDto>> replyToReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Valid @RequestBody com.example.B2C.modules.review.dto.SellerReplyRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        ReviewDto dto = reviewService.replyToReview(userId, reviewId, request.getReply());
        return ResponseEntity.ok(ApiResponse.success("Reply saved", dto));
    }
}
