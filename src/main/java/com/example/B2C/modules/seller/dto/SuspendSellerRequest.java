package com.example.B2C.modules.seller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendSellerRequest {

    @NotBlank(message = "Suspension reason is required")
    private String reason;
}
