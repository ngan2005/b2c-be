package com.example.B2C.modules.finance.entity;

import com.example.B2C.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable ledger row for the platform wallet. Every credit or debit to the
 * platform balance is recorded here for auditability.
 */
@Entity
@Table(name = "platform_wallet_txn")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformWalletTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Always points to the singleton wallet row (id = 1). */
    @Column(name = "wallet_id", nullable = false)
    @Builder.Default
    private Long walletId = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private TxnDirection direction;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /** Running balance of the wallet AFTER this transaction was applied. */
    @Column(name = "balance_after", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAfter;

    /** Why the transaction occurred. */
    @Column(name = "reason", nullable = false, length = 50)
    private String reason;

    /** Optional: links to the order that triggered this transaction. */
    @Column(name = "ref_order_id")
    private Long refOrderId;

    /** Optional: links to the seller involved (e.g. payout). */
    @Column(name = "ref_seller_id")
    private Long refSellerId;

    /** Free-text note, e.g. a bank reference or admin remark. */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
