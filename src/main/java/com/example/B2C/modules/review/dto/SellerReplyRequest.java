package com.example.B2C.modules.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for a seller to reply to a review")
public class SellerReplyRequest {

    @Size(max = 1000, message = "Reply must not exceed 1000 characters")
    @Schema(description = "Reply text to append to the review", example = "Thank you for your kind review! We look forward to serving you again.")
    private String reply;
}
