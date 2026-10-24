package com.example.B2C.modules.cart.entity;

import com.example.B2C.modules.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cart_variant", columnNames = {"cart_id", "variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Denormalised variant id — read directly without traversing the ProductVariant
     * relationship.  This field exists so that CartService / OrderServiceImpl never
     * need to call {@code cartItem.getVariant().getId()}, which would pull the
     * ProductVariant entity into the caller's package.
     *
     * <p>The column is populated by Flyway migration
     * {@code V36__cart_item_denormalised_variant_id.sql} and kept in sync on
     * INSERT/UPDATE via {@link #onCreate} / {@link #onUpdate}.
     */
    @Column(name = "variant_id_denorm")
    private Long variantId;

    /**
     * Denormalised variant display name — avoids loading the ProductVariant entity
     * just to render cart item names in the response DTO.
     */
    @Column(name = "variant_name_denorm", length = 500)
    private String variantName;

    /**
     * Denormalised product name — same reason as {@link #variantName}.
     */
    @Column(name = "product_name_denorm", length = 500)
    private String productName;

    /**
     * Denormalised image URL — same reason as {@link #variantName}.
     */
    @Column(name = "image_url_denorm", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "price_snapshot", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(name = "is_selected", nullable = false)
    @Builder.Default
    private Boolean isSelected = true;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    public Long getVariantId() {
        if (this.variantId != null) {
            return this.variantId;
        }
        return this.variant != null ? this.variant.getId() : null;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
        if (variant != null && this.variantId == null) {
            this.variantId = variant.getId();
        }
    }

    @PrePersist
    protected void onCreate() {
        if (this.addedAt == null) {
            this.addedAt = LocalDateTime.now();
        }
        if (this.variant != null && this.variantId == null) {
            this.variantId = this.variant.getId();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.variant != null && this.variantId == null) {
            this.variantId = this.variant.getId();
        }
    }
}
