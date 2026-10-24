package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.storage.StoragePort;
import com.example.B2C.common.storage.StoredAsset;
import com.example.B2C.modules.catalog.dto.ProductImageDto;
import com.example.B2C.modules.catalog.dto.ProductImageUploadResponse;
import com.example.B2C.modules.catalog.dto.UpdateProductImageRequest;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.entity.ProductImage;
import com.example.B2C.modules.catalog.repository.ProductImageRepository;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Seller-facing operations on product images.
 * <p>
 * Uploads always go through {@link StoragePort} so the storage backend
 * (Cloudinary today, maybe S3 tomorrow) stays swappable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final SellerRepository sellerRepository;
    private final StoragePort storagePort;

    /**
     * Upload a single image and attach it to {@code productId}.
     * <p>
     * Steps:
     * <ol>
     *   <li>Resolve the seller (must be ACTIVE) and verify they own the product.</li>
     *   <li>Push the bytes to the storage backend (Cloudinary).</li>
     *   <li>Persist a {@code product_image} row carrying the public URL
     *       and the storage public_id for later deletion.</li>
     *   <li>If the row is marked {@code isThumbnail=true}, also push the
     *       URL onto {@code product.thumbnail_url} so listing pages see it.</li>
     * </ol>
     */
    @Transactional
    public ProductImageUploadResponse uploadForSeller(Long userId,
                                                      Long productId,
                                                      MultipartFile file,
                                                      String altText,
                                                      Boolean isThumbnail) {
        Seller seller = getActiveSeller(userId);

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not own this product");
        }

        String purpose = "products/" + productId;
        StoredAsset asset = storagePort.upload(file, purpose);

        int nextSortOrder = productImageRepository
                .findByProductIdOrderBySortOrder(productId)
                .size();

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(asset.getSecureUrl())
                .cloudinaryPublicId(asset.getPublicId())
                .altText(altText)
                .sortOrder(nextSortOrder)
                .isThumbnail(Boolean.TRUE.equals(isThumbnail))
                .build();

        ProductImage saved = productImageRepository.save(image);

        if (Boolean.TRUE.equals(isThumbnail)) {
            product.setThumbnailUrl(asset.getSecureUrl());
            // product is managed by JPA inside the transaction; no explicit save needed.
        }

        log.info("Seller {} uploaded image {} ({} bytes) for product {}",
                seller.getId(), asset.getPublicId(), asset.getBytes(), productId);

        return ProductImageUploadResponse.builder()
                .id(saved.getId())
                .productId(productId)
                .imageUrl(saved.getImageUrl())
                .cloudinaryPublicId(saved.getCloudinaryPublicId())
                .altText(saved.getAltText())
                .sortOrder(saved.getSortOrder())
                .isThumbnail(saved.getIsThumbnail())
                .bytes(asset.getBytes())
                .format(asset.getFormat())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * Soft-delete one of the product's images.
     * <p>
     * The DB row is marked with {@code deleted_at} but kept so the seller UI
     * can offer a short "undo" action. The Cloudinary asset is also kept
     * until a separate cleanup job removes rows that have been soft-deleted
     * for longer than the undo window — until then, undo just clears the
     * flag and the asset URL is still live.
     */
    @Transactional
    public void deleteForSeller(Long userId, Long productId, Long imageId) {
        Seller seller = getActiveSeller(userId);

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not own this product");
        }

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("ProductImage", "id: " + imageId);
        }
        if (image.getDeletedAt() != null) {
            // Already soft-deleted — treat as idempotent.
            return;
        }

        image.setDeletedAt(java.time.LocalDateTime.now());
        productImageRepository.save(image);

        // If the deleted image was the thumbnail, clear product.thumbnail_url
        // so listing pages don't link to a soft-deleted image.
        if (Boolean.TRUE.equals(image.getIsThumbnail())
                && product.getThumbnailUrl() != null
                && product.getThumbnailUrl().equals(image.getImageUrl())) {
            product.setThumbnailUrl(null);
        }

        log.info("Seller {} soft-deleted image {} (public_id={}) from product {}",
                seller.getId(), imageId, image.getCloudinaryPublicId(), productId);
    }

    /**
     * Restore a previously soft-deleted product image.
     * <p>
     * Re-arms the row so it shows up in the gallery again. The Cloudinary
     * asset is untouched (the URL stays live during the undo window).
     */
    @Transactional
    public ProductImageDto restoreForSeller(Long userId, Long productId, Long imageId) {
        Seller seller = getActiveSeller(userId);

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not own this product");
        }

        ProductImage image = productImageRepository.findById(imageId)
                .filter(img -> img.getDeletedAt() != null)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("ProductImage", "id: " + imageId);
        }

        image.setDeletedAt(null);
        if (Boolean.TRUE.equals(image.getIsThumbnail())) {
            // Re-assert the thumbnail uniqueness on the product.
            productImageRepository.clearThumbnailExcept(productId, imageId);
            product.setThumbnailUrl(image.getImageUrl());
        }
        productImageRepository.save(image);

        log.info("Seller {} restored image {} on product {}",
                seller.getId(), imageId, productId);

        return ProductImageDto.from(image);
    }

    /**
     * Partially update an existing product image (alt text, sort order, thumbnail flag).
     * No file re-upload is performed.
     * <p>
     * When {@code isThumbnail=true} is set the thumbnail flag is atomically cleared
     * on all other images of the same product and the product's {@code thumbnail_url}
     * is updated to point to this image's URL.
     */
    @Transactional
    public ProductImageDto updateForSeller(Long userId,
                                           Long productId,
                                           Long imageId,
                                           UpdateProductImageRequest req) {
        Seller seller = getActiveSeller(userId);

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not own this product");
        }

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("ProductImage", "id: " + imageId);
        }

        if (req.getAltText() != null) {
            image.setAltText(req.getAltText());
        }
        if (req.getSortOrder() != null) {
            image.setSortOrder(req.getSortOrder());
        }
        if (req.getIsThumbnail() != null) {
            if (Boolean.TRUE.equals(req.getIsThumbnail())) {
                // Atomically clear all other thumbnails for this product.
                productImageRepository.clearThumbnailExcept(productId, imageId);
                image.setIsThumbnail(true);
                product.setThumbnailUrl(image.getImageUrl());
            } else {
                image.setIsThumbnail(false);
                // If this image was the product's current thumbnail, clear the URL.
                if (product.getThumbnailUrl() != null
                        && product.getThumbnailUrl().equals(image.getImageUrl())) {
                    product.setThumbnailUrl(null);
                }
            }
        }

        productImageRepository.save(image);

        log.info("Seller {} updated image {} on product {} (fields={})",
                seller.getId(), imageId, productId, req);

        return ProductImageDto.from(image);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private Seller getActiveSeller(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", "userId: " + userId));
        if (seller.getStatus() != com.example.B2C.modules.seller.entity.SellerStatus.ACTIVE) {
            throw new ForbiddenException("Seller account is not active");
        }
        return seller;
    }
}
