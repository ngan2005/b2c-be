package com.example.B2C.modules.dispute.dto;

import com.example.B2C.modules.dispute.entity.DisputeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenDisputeRequest {

    @NotNull(message = "Dispute type is required")
    private DisputeType disputeType;

    @NotBlank(message = "Description is required")
    private String description;
}
