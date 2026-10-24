package com.example.B2C.modules.finance.dto;

import com.example.B2C.modules.finance.entity.SellerNotification;
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
public class SellerNotificationDto {

    private Long id;
    private String type;
    private String title;
    private String body;
    private Long ruleId;
    private String ruleName;
    private BigDecimal rulePercent;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static SellerNotificationDto from(SellerNotification n) {
        return SellerNotificationDto.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .ruleId(n.getRuleId())
                .ruleName(n.getRuleName())
                .rulePercent(n.getRulePercent())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
