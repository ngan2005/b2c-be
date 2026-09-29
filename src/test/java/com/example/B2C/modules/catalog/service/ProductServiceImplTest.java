package com.example.B2C.modules.catalog.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.ProductDetailDto;
import com.example.B2C.modules.catalog.dto.ProductSearchRequest;
import com.example.B2C.modules.catalog.dto.ProductSummaryDto;
import com.example.B2C.modules.catalog.entity.*;
import com.example.B2C.modules.catalog.repository.*;
import com.example.B2C.modules.seller.entity.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductOptionRepository productOptionRepository;
    @Mock private ProductOptionValueRepository productOptionValueRepository;

    @InjectMocks private ProductServiceImpl productService;

    private Product sampleProduct;
    private Seller sampleSeller;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleSeller = Seller.builder()
                .id(1L)
                .shopName("Test Shop")
                .slug("test-shop")
                .ratingAvg(new BigDecimal("4.50"))
                .build();

        sampleCategory = Category.builder()
                .id(10L)
                .name("Electronics")
                .slug("electronics")
                .level(1)
                .isActive(true)
                .build();

        sampleProduct = Product.builder()
                .id(100L)
                .slug("iphone-15-pro")
                .name("iPhone 15 Pro")
                .description("Latest Apple phone")
                .brand("Apple")
                .thumbnailUrl("https://example.com/iphone.jpg")
                .minPrice(new BigDecimal("25000000"))
                .maxPrice(new BigDecimal("30000000"))
                .ratingAvg(new BigDecimal("4.80"))
                .ratingCount(120)
                .soldCount(500)
                .viewCount(10000L)
                .status(ProductStatus.ACTIVE)
                .seller(sampleSeller)
                .category(sampleCategory)
                .build();

        ReflectionTestUtils.setField(sampleProduct, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(sampleProduct, "updatedAt", LocalDateTime.now());
    }

    @Test
    @DisplayName("searchProducts - success with all filters")
    void searchProducts_withFilters_returnsPagedResults() {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .categoryId(10L)
                .sellerId(1L)
                .minPrice(new BigDecimal("10000000"))
                .maxPrice(new BigDecimal("50000000"))
                .keyword("iphone")
                .sortBy("price")
                .sortDir("asc")
                .page(0)
                .size(20)
                .build();

        Page<Product> page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 20), 1);
        when(productRepository.searchProductsWithKeyword(
                eq(ProductStatus.ACTIVE), eq(10L), eq(1L),
                eq(new BigDecimal("10000000")), eq(new BigDecimal("50000000")),
                eq("iphone"), any(Pageable.class))
        ).thenReturn(page);

        PageResponse<ProductSummaryDto> result = productService.searchProducts(request);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        ProductSummaryDto dto = result.getContent().get(0);
        assertThat(dto.getSlug()).isEqualTo("iphone-15-pro");
        assertThat(dto.getShopName()).isEqualTo("Test Shop");
        assertThat(dto.getCategoryName()).isEqualTo("Electronics");
        verify(productRepository).searchProductsWithKeyword(
                eq(ProductStatus.ACTIVE), eq(10L), eq(1L),
                eq(new BigDecimal("10000000")), eq(new BigDecimal("50000000")),
                eq("iphone"), any(Pageable.class));
    }

    @Test
    @DisplayName("searchProducts - invalid price range throws BadRequestException")
    void searchProducts_invalidPriceRange_throwsException() {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .minPrice(new BigDecimal("50000000"))
                .maxPrice(new BigDecimal("10000000"))
                .build();

        assertThatThrownBy(() -> productService.searchProducts(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("minPrice");
    }

    @Test
    @DisplayName("searchProducts - uses default pagination when null")
    void searchProducts_nullPagination_usesDefaults() {
        ProductSearchRequest request = ProductSearchRequest.builder().build();

        Page<Product> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(productRepository.searchProductsWithoutKeyword(any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        PageResponse<ProductSummaryDto> result = productService.searchProducts(request);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("searchProducts - clamps size to max 100")
    void searchProducts_largeSize_clampsTo100() {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .size(500)
                .build();

        Page<Product> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(productRepository.searchProductsWithoutKeyword(any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        productService.searchProducts(request);

        verify(productRepository).searchProductsWithoutKeyword(any(), any(), any(), any(), any(),
                argThat((Pageable p) -> p.getPageSize() == 100));
    }

    @Test
    @DisplayName("getProductDetail - success returns detail with images/variants/options")
    void getProductDetail_active_returnsDetail() {
        when(productRepository.findBySlugAndDeletedAtIsNull("iphone-15-pro"))
                .thenReturn(java.util.Optional.of(sampleProduct));
        when(productImageRepository.findByProductIdOrderBySortOrder(100L))
                .thenReturn(List.of());
        when(productVariantRepository.findAllByProductId(100L))
                .thenReturn(List.of());
        when(productOptionRepository.findByProductIdOrderBySortOrder(100L))
                .thenReturn(List.of());

        ProductDetailDto result = productService.getProductDetail("iphone-15-pro");

        assertThat(result.getSlug()).isEqualTo("iphone-15-pro");
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getShopName()).isEqualTo("Test Shop");
    }

    @Test
    @DisplayName("getProductDetail - not found throws ResourceNotFoundException")
    void getProductDetail_notFound_throwsException() {
        when(productRepository.findBySlugAndDeletedAtIsNull("unknown"))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> productService.getProductDetail("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    @DisplayName("getProductDetail - hidden product throws ResourceNotFoundException")
    void getProductDetail_hiddenProduct_throwsException() {
        sampleProduct.setStatus(ProductStatus.HIDDEN);
        when(productRepository.findBySlugAndDeletedAtIsNull("iphone-15-pro"))
                .thenReturn(java.util.Optional.of(sampleProduct));

        assertThatThrownBy(() -> productService.getProductDetail("iphone-15-pro"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("incrementViewCount - existing product increments counter")
    void incrementViewCount_existingProduct_increments() {
        sampleProduct.setViewCount(100L);
        when(productRepository.findById(100L)).thenReturn(java.util.Optional.of(sampleProduct));

        productService.incrementViewCount(100L);

        assertThat(sampleProduct.getViewCount()).isEqualTo(101L);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("incrementViewCount - non-existing product does nothing")
    void incrementViewCount_nonExisting_doesNothing() {
        when(productRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        productService.incrementViewCount(999L);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("getFeaturedProducts - returns sorted by soldCount")
    void getFeaturedProducts_returnsSorted() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 20), 1);
        when(productRepository.findFeatured(any(Pageable.class))).thenReturn(page);

        PageResponse<ProductSummaryDto> result = productService.getFeaturedProducts(0, 20);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findFeatured(argThat(p ->
                p.getSort().getOrderFor("soldCount") != null
                && p.getSort().getOrderFor("soldCount").getDirection().isDescending()));
    }

    @Test
    @DisplayName("getProductsBySeller - returns active products for seller")
    void getProductsBySeller_returnsActiveProducts() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 20), 1);
        when(productRepository.findActiveBySeller(eq(1L), any(Pageable.class))).thenReturn(page);

        PageResponse<ProductSummaryDto> result = productService.getProductsBySeller(1L, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getShopName()).isEqualTo("Test Shop");
    }
}
