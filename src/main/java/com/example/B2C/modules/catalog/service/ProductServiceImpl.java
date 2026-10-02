package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.*;
import com.example.B2C.modules.catalog.entity.*;
import com.example.B2C.modules.catalog.repository.*;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionValueRepository productOptionValueRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "price", "minPrice",
            "sold", "soldCount",
            "rating", "ratingAvg",
            "created", "createdAt",
            "name", "name"
    );

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    @Override
    public PageResponse<ProductSummaryDto> searchProducts(ProductSearchRequest request) {
        validatePriceRange(request.getMinPrice(), request.getMaxPrice());

        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = clampSize(request.getSize() != null ? request.getSize() : 20);

        Pageable pageable = buildPageable(page, size, request.getSortBy(), request.getSortDir());

        Page<Product> productPage;
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            productPage = productRepository.searchProductsWithoutKeyword(
                    ProductStatus.ACTIVE,
                    request.getCategoryId(),
                    request.getSellerId(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    pageable
            );
        } else {
            productPage = productRepository.searchProductsWithKeyword(
                    ProductStatus.ACTIVE,
                    request.getCategoryId(),
                    request.getSellerId(),
                    request.getMinPrice(),
                    request.getMaxPrice(),
                    keyword.trim(),
                    pageable
            );
        }

        Page<ProductSummaryDto> dtoPage = productPage.map(this::toSummaryDto);
        return PageResponse.of(dtoPage);
    }

    @Override
    public ProductDetailDto getProductDetail(String slug) {
        Product product = productRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug: " + slug));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product", "slug: " + slug);
        }

        return toDetailDto(product);
    }

    @Override
    public PageResponse<ProductSummaryDto> getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                Sort.by(Sort.Direction.DESC, "soldCount")
        );
        Page<Product> productPage = productRepository.findFeatured(pageable);
        return PageResponse.of(productPage.map(this::toSummaryDto));
    }

    @Override
    public PageResponse<ProductSummaryDto> getProductsBySeller(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Product> productPage = productRepository.findActiveBySeller(sellerId, pageable);
        return PageResponse.of(productPage.map(this::toSummaryDto));
    }

    @Override
    @Transactional
    public void incrementViewCount(Long productId) {
        productRepository.findById(productId).ifPresent(p -> {
            p.setViewCount(p.getViewCount() + 1);
            productRepository.save(p);
        });
    }

    // ===================== Seller-only operations =====================

    @Override
    public PageResponse<ProductSummaryDto> listProductsForCurrentSeller(Long sellerId, ProductStatus status,
                                                                       String keyword, int page, int size) {
        Seller seller = getActiveSeller(sellerId);
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        String trimmed = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Product> productPage = productRepository.findAllBySeller(seller, status, trimmed, pageable);
        return PageResponse.of(productPage.map(this::toSummaryDto));
    }

    @Override
    public ProductDetailDto getProductForSeller(Long sellerId, Long productId) {
        Seller seller = getActiveSeller(sellerId);
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        ensureOwnedBySeller(product, seller);
        return toDetailDto(product);
    }

    @Override
    @Transactional
    public ProductDetailDto createProductForSeller(Long sellerId, CreateProductRequest request) {
        Seller seller = getActiveSeller(sellerId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id: " + request.getCategoryId()));

        String slug = resolveSlug(request.getSlug(), request.getName());

        if (productRepository.existsBySlug(slug)) {
            throw new ConflictException("Slug already exists: " + slug);
        }

        ProductStatus desiredStatus = parseStatus(request.getStatus(), ProductStatus.DRAFT);
        if (desiredStatus == ProductStatus.BANNED) {
            throw new BadRequestException("Cannot create a product with BANNED status");
        }

        Product product = Product.builder()
                .seller(seller)
                .category(category)
                .name(request.getName().trim())
                .slug(slug)
                .brand(request.getBrand())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .weightGram(request.getWeightGram())
                .lengthCm(request.getLengthCm())
                .widthCm(request.getWidthCm())
                .heightCm(request.getHeightCm())
                .status(desiredStatus)
                .build();

        Product saved = productRepository.save(product);
        seller.setTotalProduct(seller.getTotalProduct() == null ? 1 : seller.getTotalProduct() + 1);
        sellerRepository.save(seller);

        log.info("Seller {} created product {} (slug={})", sellerId, saved.getId(), saved.getSlug());
        return toDetailDto(saved);
    }

    @Override
    @Transactional
    public ProductDetailDto updateProductForSeller(Long sellerId, Long productId, UpdateProductRequest request) {
        Seller seller = getActiveSeller(sellerId);
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        ensureOwnedBySeller(product, seller);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id: " + request.getCategoryId()));

        validatePriceRange(request.getMinPrice(), request.getMaxPrice());

        product.setName(request.getName().trim());
        product.setCategory(category);
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setThumbnailUrl(request.getThumbnailUrl());
        product.setMinPrice(request.getMinPrice());
        product.setMaxPrice(request.getMaxPrice());
        product.setWeightGram(request.getWeightGram());
        product.setLengthCm(request.getLengthCm());
        product.setWidthCm(request.getWidthCm());
        product.setHeightCm(request.getHeightCm());

        Product saved = productRepository.save(product);
        return toDetailDto(saved);
    }

    @Override
    @Transactional
    public void deleteProductForSeller(Long sellerId, Long productId) {
        Seller seller = getActiveSeller(sellerId);
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        ensureOwnedBySeller(product, seller);

        product.setDeletedAt(LocalDateTime.now());
        product.setStatus(ProductStatus.HIDDEN);
        productRepository.save(product);

        if (seller.getTotalProduct() != null && seller.getTotalProduct() > 0) {
            seller.setTotalProduct(seller.getTotalProduct() - 1);
            sellerRepository.save(seller);
        }
    }

    @Override
    @Transactional
    public ProductDetailDto updateProductStatusForSeller(Long sellerId, Long productId, ProductStatus status) {
        Seller seller = getActiveSeller(sellerId);
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        ensureOwnedBySeller(product, seller);

        if (status == ProductStatus.BANNED) {
            throw new BadRequestException("Sellers cannot self-BAN; contact admin");
        }

        product.setStatus(status);
        Product saved = productRepository.save(product);
        return toDetailDto(saved);
    }

    // ===================== Helpers =====================

    private Seller getActiveSeller(Long userId) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user id: " + userId));
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new ForbiddenException("Seller profile is not active");
        }
        return seller;
    }

    private void ensureOwnedBySeller(Product product, Seller seller) {
        if (product.getSeller() == null || !seller.getId().equals(product.getSeller().getId())) {
            throw new ForbiddenException("You do not own this product");
        }
    }

    private ProductStatus parseStatus(String raw, ProductStatus fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return ProductStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid product status: " + raw);
        }
    }

    private String resolveSlug(String requestedSlug, String name) {
        if (requestedSlug != null && !requestedSlug.isBlank()) {
            String trimmed = requestedSlug.trim();
            if (productRepository.existsBySlug(trimmed)) {
                throw new ConflictException("Slug already exists: " + trimmed);
            }
            return trimmed;
        }
        String base = slugify(name == null ? "" : name);
        if (base.isEmpty()) {
            base = "product";
        }
        String candidate = base;
        int suffix = 1;
        while (productRepository.existsBySlug(candidate)) {
            suffix++;
            candidate = base + "-" + suffix;
        }
        return candidate;
    }

    private static String slugify(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String stripped = NON_LATIN.matcher(normalized).replaceAll("");
        return stripped.toLowerCase(Locale.ENGLISH);
    }

    private ProductDetailDto toDetailDto(Product product) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrder(product.getId());
        List<ProductVariant> variants = productVariantRepository.findAllByProductId(product.getId());
        List<ProductOption> options = productOptionRepository.findByProductIdOrderBySortOrder(product.getId());

        List<ProductOptionValue> optionValues = options.isEmpty()
                ? List.of()
                : productOptionValueRepository.findByOptionIds(
                        options.stream().map(ProductOption::getId).toList());

        Map<Long, List<ProductOptionValue>> valuesByOptionId = optionValues.stream()
                .collect(Collectors.groupingBy(v -> v.getOption().getId()));

        List<ProductOptionDto> optionDtos = options.stream().map(opt -> {
            List<ProductOptionValueDto> valueDtos = valuesByOptionId
                    .getOrDefault(opt.getId(), List.of())
                    .stream()
                    .map(v -> ProductOptionValueDto.builder()
                            .id(v.getId())
                            .value(v.getValue())
                            .imageUrl(v.getImageUrl())
                            .sortOrder(v.getSortOrder())
                            .build())
                    .collect(Collectors.toList());

            return ProductOptionDto.builder()
                    .id(opt.getId())
                    .name(opt.getName())
                    .sortOrder(opt.getSortOrder())
                    .values(valueDtos)
                    .build();
        }).collect(Collectors.toList());

        return ProductDetailDto.builder()
                .id(product.getId())
                .slug(product.getSlug())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .shopName(product.getSeller() != null ? product.getSeller().getShopName() : null)
                .shopSlug(product.getSeller() != null ? product.getSeller().getSlug() : null)
                .sellerRatingAvg(product.getSeller() != null ? product.getSeller().getRatingAvg() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : null)
                .minPrice(product.getMinPrice())
                .maxPrice(product.getMaxPrice())
                .thumbnailUrl(product.getThumbnailUrl())
                .images(images.stream().map(img -> ProductImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .altText(img.getAltText())
                        .sortOrder(img.getSortOrder())
                        .isThumbnail(img.getIsThumbnail())
                        .build()).collect(Collectors.toList()))
                .variants(variants.stream().map(v -> ProductVariantDto.builder()
                        .id(v.getId())
                        .sku(v.getSku())
                        .variantName(v.getVariantName())
                        .price(v.getPrice())
                        .salePrice(v.getSalePrice())
                        .stockQuantity(v.getStockQuantity())
                        .soldCount(v.getSoldCount())
                        .imageUrl(v.getImageUrl())
                        .weightGram(v.getWeightGram())
                        .barcode(v.getBarcode())
                        .isActive(v.getIsActive())
                        .build()).collect(Collectors.toList()))
                .options(optionDtos)
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .soldCount(product.getSoldCount())
                .viewCount(product.getViewCount())
                .weightGram(product.getWeightGram())
                .lengthCm(product.getLengthCm())
                .widthCm(product.getWidthCm())
                .heightCm(product.getHeightCm())
                .status(product.getStatus().name())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private ProductSummaryDto toSummaryDto(Product product) {
        return ProductSummaryDto.builder()
                .id(product.getId())
                .slug(product.getSlug())
                .name(product.getName())
                .minPrice(product.getMinPrice())
                .maxPrice(product.getMaxPrice())
                .thumbnailUrl(product.getThumbnailUrl())
                .brand(product.getBrand())
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .soldCount(product.getSoldCount())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .shopName(product.getSeller() != null ? product.getSeller().getShopName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        String field = sortBy != null ? SORT_FIELD_MAP.getOrDefault(sortBy.toLowerCase(), "createdAt") : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }

    private int clampSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, 100);
    }

    private void validatePriceRange(java.math.BigDecimal min, java.math.BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BadRequestException("minPrice must be less than or equal to maxPrice");
        }
    }
}