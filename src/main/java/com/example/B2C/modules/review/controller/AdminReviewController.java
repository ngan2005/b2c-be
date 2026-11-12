package com.example.B2C.modules.review.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.review.dto.AdminReviewStatsDto;
import com.example.B2C.modules.review.dto.ReviewDto;
import com.example.B2C.modules.review.dto.UpdateReviewStatusRequest;
import com.example.B2C.modules.review.entity.ReviewStatus;
import com.example.B2C.modules.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Reviews", description = "Admin product review moderation and analytics APIs")
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "List all reviews for admin moderation",
            description = "Get paginated reviews across platform with filters by status, rating, or search keyword")
    public ResponseEntity<ApiResponse<PageResponse<ReviewDto>>> listReviews(
            @Parameter(description = "Filter by status: VISIBLE, HIDDEN") @RequestParam(required = false) ReviewStatus status,
            @Parameter(description = "Filter by star rating: 1-5") @RequestParam(required = false) Integer rating,
            @Parameter(description = "Search keyword in comment, user name, product name, or shop name") @RequestParam(required = false) String keyword,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 15, max 100)") @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.listForAdmin(status, rating, keyword, page, size)));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform review analytics & metrics",
            description = "Returns counts by rating, visibility status, and overall average rating")
    public ResponseEntity<ApiResponse<AdminReviewStatsDto>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getAdminStats()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update review visibility status",
            description = "Admin can hide a toxic/spam review or make it visible again")
    public ResponseEntity<ApiResponse<ReviewDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewStatusRequest request) {
        ReviewDto updated = reviewService.updateStatusByAdmin(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Trạng thái đánh giá đã được cập nhật", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review",
            description = "Permanently remove a review and recalculate product rating")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteByAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá đã được xóa thành công", null));
    }
}
