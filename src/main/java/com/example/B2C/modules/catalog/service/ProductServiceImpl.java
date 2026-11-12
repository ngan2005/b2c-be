package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ConflictException;
import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.*;
import com.example.B2C.modules.catalog.entity.*;
import com.example.B2C.modules.catalog.repository.*;
import com.example.B2C.modules.catalog.util.SearchQueryUtils;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.realtime.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final VariantOptionValueRepository variantOptionValueRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final UserPresenceService userPresenceService;

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "price", "minPrice",
            "sold", "soldCount",
            "rating", "ratingAvg",
            "created", "createdAt",
            "name", "name",
            "relevance", "createdAt");

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
                    request.getBrand(),
                    request.getMinRating(),
                    pageable);
        } else {
            String trimmed = keyword.trim();
            String tsQuery = SearchQueryUtils.toPrefixTsQuery(trimmed);
            Page<Product> ftsPage = null;
            Pageable ftsPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            try {
                ftsPage = productRepository.searchProductsFulltext(
                        ProductStatus.ACTIVE.name(),
                        request.getCategoryId(),
                        request.getSellerId(),
                        request.getMinPrice(),
                        request.getMaxPrice(),
                        request.getBrand(),
                        request.getMinRating(),
                        trimmed,
                        tsQuery,
                        request.getSortBy(),
                        request.getSortDir(),
                        ftsPageable);
            } catch (Exception e) {
                log.warn("FTS search failed for keyword '{}' (tsQuery: '{}'): {}", trimmed, tsQuery, e.getMessage());
            }

            if (ftsPage != null && !ftsPage.isEmpty()) {
                productPage = ftsPage;
            } else {
                productPage = productRepository.searchProductsWithKeywordLike(
                        ProductStatus.ACTIVE,
                        request.getCategoryId(),
                        request.getSellerId(),
                        request.getMinPrice(),
                        request.getMaxPrice(),
                        request.getBrand(),
                        request.getMinRating(),
                        trimmed,
                        pageable);
            }
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

    // P1.1 — Look up by numeric ID (mobile app)
    @Override
    public ProductDetailDto getProductDetailById(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + id));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException("Product", "id: " + id);
        }
        return toDetailDto(product);
    }

    @Override
    public PageResponse<ProductSummaryDto> getFeaturedProducts(int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                Sort.by(Sort.Direction.DESC, "soldCount"));
        Page<Product> productPage = productRepository.findFeatured(pageable);
        return PageResponse.of(productPage.map(this::toSummaryDto));
    }

    @Override
    public PageResponse<ProductSummaryDto> getProductsBySeller(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
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
                Sort.by(Sort.Direction.DESC, "createdAt"));
        String trimmed = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Product> productPage;
        if (trimmed == null) {
            productPage = productRepository.findAllBySellerWithoutKeyword(seller, status, pageable);
        } else {
            String tsQuery = SearchQueryUtils.toPrefixTsQuery(trimmed);
            Pageable ftsPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            productPage = productRepository.searchProductsSellerFulltext(
                    seller.getId(),
                    status != null ? status.name() : null,
                    trimmed,
                    tsQuery,
                    ftsPageable);
        }
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

        saveOptionsAndVariants(saved, request.getOptions(), request.getVariants());

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
        saveOptionsAndVariants(saved, request.getOptions(), request.getVariants());
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
    public ProductDetailDto restoreProductForSeller(Long sellerId, Long productId) {
        Seller seller = getActiveSeller(sellerId);
        // Only the seller that owns the product can restore it, and only if it
        // is currently soft-deleted.
        Product product = productRepository.findById(productId)
                .filter(p -> p.getDeletedAt() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        ensureOwnedBySeller(product, seller);

        product.setDeletedAt(null);
        // Keep HIDDEN as the safe default — the seller can re-activate
        // through the status PATCH endpoint once they're ready.
        productRepository.save(product);

        seller.setTotalProduct((seller.getTotalProduct() == null ? 0 : seller.getTotalProduct()) + 1);
        sellerRepository.save(seller);

        return getProductForSeller(sellerId, productId);
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

        Seller seller = product.getSeller();
        Integer shopProductCount = null;
        Boolean shopIsOnline = false;
        if (seller != null) {
            shopProductCount = (int) productRepository.countBySellerIdAndStatusAndDeletedAtIsNull(seller.getId(),
                    ProductStatus.ACTIVE);
            if (seller.getUser() != null && userPresenceService != null) {
                shopIsOnline = userPresenceService.isUserOnline(seller.getUser().getId());
            }
        }

        return ProductDetailDto.builder()
                .id(product.getId())
                .slug(product.getSlug())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .sellerId(seller != null ? seller.getId() : null)
                .shopName(seller != null ? seller.getShopName() : null)
                .shopSlug(seller != null ? seller.getSlug() : null)
                .shopLogoUrl(seller != null ? seller.getLogoUrl() : null)
                .sellerRatingAvg(seller != null ? seller.getRatingAvg() : null)
                .shopRatingCount(seller != null ? seller.getRatingCount() : null)
                .shopProductCount(shopProductCount)
                .shopCreatedAt(seller != null ? seller.getCreatedAt() : null)
                .shopIsOnline(shopIsOnline)
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
                .variants(variants.stream().map(v -> {
                    List<ProductOptionValue> vOptValues = productOptionValueRepository.findByVariantId(v.getId());
                    Map<String, String> attrs = new java.util.LinkedHashMap<>();
                    for (ProductOptionValue pov : vOptValues) {
                        if (pov.getOption() != null) {
                            attrs.put(pov.getOption().getName(), pov.getValue());
                        }
                    }
                    return ProductVariantDto.builder()
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
                            .optionValueIds(vOptValues.stream().map(ProductOptionValue::getId).collect(Collectors.toList()))
                            .attributes(attrs)
                            .build();
                }).collect(Collectors.toList()))
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
                .status(product.getStatus() != null ? product.getStatus().name() : "ACTIVE")
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
        if (size <= 0)
            return 20;
        return Math.min(size, 100);
    }

    private void validatePriceRange(java.math.BigDecimal min, java.math.BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BadRequestException("minPrice must be less than or equal to maxPrice");
        }
    }

    // ===================== Admin-only =====================

    @Override
    public PageResponse<AdminProductSummaryDto> listProductsForAdmin(ProductStatus status, Long sellerId,
            String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                clampSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        String trimmed = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        org.springframework.data.domain.Page<Product> productPage;
        if (trimmed == null) {
            if (status != null) {
                productPage = productRepository.searchProductsWithoutKeyword(
                        status, null, sellerId, null, null, null, null, pageable);
            } else {
                productPage = productRepository.findAllDeletedAtIsNull(pageable);
            }
        } else {
            String tsQuery = SearchQueryUtils.toPrefixTsQuery(trimmed);
            Pageable ftsPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            productPage = productRepository.searchProductsFulltext(
                    status != null ? status.name() : null,
                    null,
                    sellerId,
                    null,
                    null,
                    null,
                    null,
                    trimmed,
                    tsQuery,
                    null,
                    null,
                    ftsPageable);
        }
        return PageResponse.of(productPage.map(this::toAdminSummaryDto));
    }

    @Override
    public ProductDetailDto getProductForAdmin(Long productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        return toDetailDto(product);
    }

    @Override
    @Transactional
    public ProductDetailDto updateProductStatusForAdmin(Long productId, ProductStatus status) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id: " + productId));
        product.setStatus(status);
        Product saved = productRepository.save(product);
        log.info("Admin updated product {} status to {}", productId, status);
        return toDetailDto(saved);
    }

    private AdminProductSummaryDto toAdminSummaryDto(Product product) {
        return AdminProductSummaryDto.builder()
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
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private void saveOptionsAndVariants(Product product, List<ProductOptionRequest> optionRequests, List<ProductVariantRequest> variantRequests) {
        // 1. Process options if provided
        Map<String, ProductOptionValue> optionValueLookup = new HashMap<>();
        if (optionRequests != null && !optionRequests.isEmpty()) {
            List<ProductOption> existingOptions = productOptionRepository.findByProductIdOrderBySortOrder(product.getId());
            Map<String, ProductOption> existingOptMap = existingOptions.stream()
                    .collect(Collectors.toMap(o -> o.getName().trim().toLowerCase(), o -> o, (a, b) -> a));

            int optSort = 0;
            for (ProductOptionRequest optReq : optionRequests) {
                if (optReq.getName() == null || optReq.getName().isBlank()) continue;
                String optName = optReq.getName().trim();
                ProductOption option = existingOptMap.get(optName.toLowerCase());
                if (option == null) {
                    option = ProductOption.builder()
                            .product(product)
                            .name(optName)
                            .sortOrder(optSort++)
                            .build();
                    option = productOptionRepository.save(option);
                }

                if (optReq.getValues() != null) {
                    List<ProductOptionValue> existingVals = productOptionValueRepository.findByOptionIdOrderBySortOrderAsc(option.getId());
                    Map<String, ProductOptionValue> existingValMap = existingVals.stream()
                            .collect(Collectors.toMap(v -> v.getValue().trim().toLowerCase(), v -> v, (a, b) -> a));

                    int valSort = 0;
                    for (String valStr : optReq.getValues()) {
                        if (valStr == null || valStr.isBlank()) continue;
                        String trimmedVal = valStr.trim();
                        ProductOptionValue pov = existingValMap.get(trimmedVal.toLowerCase());
                        if (pov == null) {
                            pov = ProductOptionValue.builder()
                                    .option(option)
                                    .value(trimmedVal)
                                    .sortOrder(valSort++)
                                    .build();
                            pov = productOptionValueRepository.save(pov);
                        }
                        optionValueLookup.put(optName.toLowerCase() + ":::" + trimmedVal.toLowerCase(), pov);
                        optionValueLookup.put(trimmedVal.toLowerCase(), pov);
                    }
                }
            }
        }

        // 2. Process variants
        if (variantRequests != null && !variantRequests.isEmpty()) {
            BigDecimal minPrice = null;
            BigDecimal maxPrice = null;
            int variantIdx = 0;

            for (ProductVariantRequest vReq : variantRequests) {
                variantIdx++;
                ProductVariant variant = null;
                if (vReq.getId() != null) {
                    variant = productVariantRepository.findById(vReq.getId()).orElse(null);
                }

                String sku = (vReq.getSku() != null && !vReq.getSku().isBlank())
                        ? vReq.getSku().trim()
                        : generateSku(product.getSlug(), variantIdx);

                if (variant == null) {
                    if (productVariantRepository.existsBySku(sku)) {
                        sku = sku + "-" + (System.currentTimeMillis() % 10000);
                    }
                    variant = ProductVariant.builder()
                            .product(product)
                            .sku(sku)
                            .build();
                } else {
                    if (vReq.getSku() != null && !vReq.getSku().isBlank() && !vReq.getSku().equals(variant.getSku())) {
                        if (!productVariantRepository.existsBySku(vReq.getSku().trim())) {
                            variant.setSku(vReq.getSku().trim());
                        }
                    }
                }

                String vName = (vReq.getVariantName() != null && !vReq.getVariantName().isBlank())
                        ? vReq.getVariantName().trim()
                        : "Phân loại " + variantIdx;

                BigDecimal price = vReq.getPrice() != null ? vReq.getPrice() : (product.getMinPrice() != null ? product.getMinPrice() : BigDecimal.ZERO);
                int stock = vReq.getStockQuantity() != null ? Math.max(0, vReq.getStockQuantity()) : 0;
                int weight = vReq.getWeightGram() != null ? vReq.getWeightGram() : (product.getWeightGram() != null ? product.getWeightGram() : 250);

                variant.setVariantName(vName);
                variant.setPrice(price);
                variant.setSalePrice(vReq.getSalePrice());
                variant.setStockQuantity(stock);
                variant.setWeightGram(weight);
                if (vReq.getImageUrl() != null && !vReq.getImageUrl().isBlank()) {
                    variant.setImageUrl(vReq.getImageUrl());
                }
                variant.setIsActive(true);

                ProductVariant savedVariant = productVariantRepository.save(variant);

                if (minPrice == null || price.compareTo(minPrice) < 0) minPrice = price;
                if (maxPrice == null || price.compareTo(maxPrice) > 0) maxPrice = price;

                // Link variant_option_value
                if (vReq.getOptionValues() != null && !vReq.getOptionValues().isEmpty()) {
                    variantOptionValueRepository.deleteByVariantId(savedVariant.getId());
                    for (String optValStr : vReq.getOptionValues()) {
                        if (optValStr == null || optValStr.isBlank()) continue;
                        ProductOptionValue matchedPov = optionValueLookup.get(optValStr.trim().toLowerCase());
                        if (matchedPov != null) {
                            VariantOptionValue vov = VariantOptionValue.builder()
                                    .id(new VariantOptionValueId(savedVariant.getId(), matchedPov.getId()))
                                    .variant(savedVariant)
                                    .optionValue(matchedPov)
                                    .build();
                            variantOptionValueRepository.save(vov);
                        }
                    }
                }
            }

            if (minPrice != null && maxPrice != null) {
                product.setMinPrice(minPrice);
                product.setMaxPrice(maxPrice);
                productRepository.save(product);
            }
        } else {
            List<ProductVariant> existing = productVariantRepository.findAllByProductId(product.getId());
            if (existing.isEmpty()) {
                String sku = generateSku(product.getSlug(), 1);
                ProductVariant defaultVariant = ProductVariant.builder()
                        .product(product)
                        .sku(sku)
                        .variantName("Mặc định")
                        .price(product.getMinPrice() != null ? product.getMinPrice() : BigDecimal.valueOf(100000))
                        .salePrice(null)
                        .stockQuantity(100)
                        .soldCount(0)
                        .weightGram(product.getWeightGram() != null ? product.getWeightGram() : 250)
                        .isActive(true)
                        .build();
                productVariantRepository.save(defaultVariant);
            }
        }
    }

    private String generateSku(String slug, int idx) {
        String clean = (slug != null ? slug.toUpperCase().replaceAll("[^A-Z0-9]", "-") : "SKU");
        if (clean.length() > 20) clean = clean.substring(0, 20);
        return clean + "-V" + idx + "-" + (System.currentTimeMillis() % 10000);
    }
}