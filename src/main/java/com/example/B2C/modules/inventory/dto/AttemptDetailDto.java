package com.example.B2C.modules.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptDetailDto {
    private int attemptIndex;
    private String status;
    private boolean isSuccess;
    private long latencyMs;
    private String message;
}

