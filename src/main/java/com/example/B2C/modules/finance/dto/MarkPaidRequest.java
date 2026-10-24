package com.example.B2C.modules.finance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for marking a payout as paid")
public class MarkPaidRequest {

    @Schema(description = "Bank transfer reference / transaction ID", example = "BIDV-2024-001234")
    @NotBlank(message = "bankReference is required")
    @Size(max = 100, message = "bankReference must be at most 100 characters")
    private String bankReference;
}
