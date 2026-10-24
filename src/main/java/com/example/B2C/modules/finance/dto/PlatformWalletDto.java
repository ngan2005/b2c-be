package com.example.B2C.modules.finance.dto;

import com.example.B2C.modules.finance.entity.PlatformWallet;
import com.example.B2C.modules.finance.entity.PlatformWalletTxn;
import com.example.B2C.modules.finance.entity.TxnDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Platform wallet snapshot with recent transactions")
public class PlatformWalletDto {

    @Schema(description = "Current available balance")
    private BigDecimal balance;

    @Schema(description = "Total gross credits (commission income) ever accumulated")
    private BigDecimal totalCredited;

    @Schema(description = "Total gross debits (seller payouts) ever made")
    private BigDecimal totalDebited;

    @Schema(description = "When the balance was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "Most recent wallet transactions")
    private List<WalletTxnDto> recentTransactions;

    public static PlatformWalletDto from(PlatformWallet wallet, List<PlatformWalletTxn> recent) {
        return PlatformWalletDto.builder()
                .balance(wallet.getBalance())
                .totalCredited(wallet.getTotalCredited())
                .totalDebited(wallet.getTotalDebited())
                .updatedAt(wallet.getUpdatedAt())
                .recentTransactions(recent.stream().map(WalletTxnDto::from).toList())
                .build();
    }
}
