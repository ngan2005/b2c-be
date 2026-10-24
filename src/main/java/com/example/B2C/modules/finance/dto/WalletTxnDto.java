package com.example.B2C.modules.finance.dto;

import com.example.B2C.modules.finance.entity.PlatformWalletTxn;
import com.example.B2C.modules.finance.entity.TxnDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Single wallet transaction DTO")
public class WalletTxnDto {

    @Schema(description = "Transaction ID")
    private Long id;

    @Schema(description = "CREDIT or DEBIT")
    private TxnDirection direction;

    @Schema(description = "Amount of this transaction")
    private BigDecimal amount;

    @Schema(description = "Wallet balance immediately after this transaction")
    private BigDecimal balanceAfter;

    @Schema(description = "Why the transaction occurred")
    private String reason;

    @Schema(description = "Related order ID")
    private Long refOrderId;

    @Schema(description = "Related seller ID")
    private Long refSellerId;

    @Schema(description = "Free-text note")
    private String note;

    @Schema(description = "When the transaction was recorded")
    private LocalDateTime createdAt;

    public static WalletTxnDto from(PlatformWalletTxn t) {
        return WalletTxnDto.builder()
                .id(t.getId())
                .direction(t.getDirection())
                .amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter())
                .reason(t.getReason())
                .refOrderId(t.getRefOrderId())
                .refSellerId(t.getRefSellerId())
                .note(t.getNote())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
