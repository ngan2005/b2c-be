package com.example.B2C.modules.review.dto;

import com.example.B2C.modules.review.entity.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewStatusRequest {

    @NotNull(message = "Review status is required")
    private ReviewStatus status;
}
