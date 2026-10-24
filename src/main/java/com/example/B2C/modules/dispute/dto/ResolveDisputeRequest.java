package com.example.B2C.modules.dispute.dto;

import com.example.B2C.modules.dispute.entity.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResolveDisputeRequest {

    /**
     * Must be one of RESOLVED_REFUND, RESOLVED_REJECT, RESOLVED_PARTIAL.
     */
    @NotNull(message = "Resolution status is required")
    private DisputeStatus status;

    private String resolutionNote;

    /**
     * Refund amount — required for RESOLVED_REFUND and RESOLVED_PARTIAL.
     */
    private BigDecimal refundAmount;
}
