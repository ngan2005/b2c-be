package com.example.B2C.modules.finance.dto;

import com.example.B2C.modules.finance.entity.TxnDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for a manual wallet balance adjustment by an admin")
public class AdjustmentRequest {

    @Schema(description = "Adjustment amount", example = "500000.00")
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    private BigDecimal amount;

    @Schema(description = "CREDIT or DEBIT")
    @NotNull(message = "direction is required")
    private TxnDirection direction;

    @Schema(description = "Reason for the adjustment", example = "Platform promo credit")
    @Size(max = 50, message = "reason must be at most 50 characters")
    private String reason;

    @Schema(description = "Optional free-text note")
    @Size(max = 500, message = "note must be at most 500 characters")
    private String note;
}
