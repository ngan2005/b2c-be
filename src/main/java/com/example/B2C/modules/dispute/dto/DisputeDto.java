package com.example.B2C.modules.dispute.dto;

import com.example.B2C.modules.dispute.entity.DisputeStatus;
import com.example.B2C.modules.dispute.entity.DisputeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeDto {
    private Long id;
    private Long orderId;
    private String orderCode;
    private Long openedBy;
    private String openedByEmail;
    private DisputeType disputeType;
    private String description;
    private DisputeStatus status;
    private String resolutionNote;
    private BigDecimal refundAmount;
    private Long resolvedBy;
    private String resolvedByEmail;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
