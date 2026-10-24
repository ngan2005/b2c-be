package com.example.B2C.modules.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_thumbnail", nullable = false)
    @Builder.Default
    private Boolean isThumbnail = false;

    /**
     * Cloudinary public_id of the uploaded asset. Populated by
     * {@code ProductImageService} when the seller uploads through the
     * Cloudinary-backed endpoint. Null for legacy rows pre-dating
     * Cloudinary integration.
     */
    @Column(name = "cloudinary_public_id")
    private String cloudinaryPublicId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    /**
     * Soft-delete marker. Null when the image is live. When set, the row is
     * hidden from listing endpoints but kept (along with the Cloudinary
     * asset reference) so a short "undo delete" action can restore it.
     */
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;

    @jakarta.persistence.PrePersist
    void onCreate() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
