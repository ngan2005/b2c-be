package com.example.B2C.modules.catalog.repository;

import com.example.B2C.modules.catalog.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("""
        SELECT img FROM ProductImage img
        WHERE img.product.id = :productId AND img.deletedAt IS NULL
        ORDER BY img.sortOrder ASC, img.id ASC
        """)
    List<ProductImage> findByProductIdOrderBySortOrder(@Param("productId") Long productId);

    /**
     * Clears the thumbnail flag on all images of a product except one.
     * Used atomically when promoting a new image to thumbnail so that
     * at most one {@code isThumbnail=true} row exists per product.
     */
    @Modifying
    @Query("update ProductImage pi set pi.isThumbnail = false " +
           "where pi.product.id = :productId and pi.id <> :exceptId and pi.deletedAt IS NULL")
    int clearThumbnailExcept(@Param("productId") Long productId,
                             @Param("exceptId") Long exceptId);
}
