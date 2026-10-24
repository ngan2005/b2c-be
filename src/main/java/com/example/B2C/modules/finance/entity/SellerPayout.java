package com.example.B2C.modules.finance.entity;

import com.example.B2C.common.entity.BaseEntity;
import com.example.B2C.modules.seller.entity.Seller;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payout record for a seller: gross order amount minus commission
 * = net amount owed to the seller.
 *
 * <p>Each completed order generates exactly one PENDING row. When the admin marks
 * it as paid the status moves to PAID and a DEBIT transaction is written to
 * {@link PlatformWallet}.
 */
@Entity
@Table(name = "seller_payout")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPayout extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    /** May be null for aggregated payouts (future extension). */
    @Column(name = "order_id")
    private Long orderId;

    /** Order gross (subtotal + shipping, before commission). */
    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    /** Platform commission taken from this order. */
    @Column(name = "commission_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    /** grossAmount - commissionAmount. Always >= 0 (enforced by DB check). */
    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    /** Filled by admin when recording the bank transfer. */
    @Column(name = "bank_reference", length = 100)
    private String bankReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** Admin user id who marked this payout as paid. */
    @Column(name = "paid_by")
    private Long paidBy;

    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;
}
