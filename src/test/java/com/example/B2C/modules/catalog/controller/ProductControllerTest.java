package com.example.B2C.modules.catalog.controller;

import com.example.B2C.common.exception.GlobalExceptionHandler;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.dto.ProductDetailDto;
import com.example.B2C.modules.catalog.dto.ProductSearchRequest;
import com.example.B2C.modules.catalog.dto.ProductSummaryDto;
import com.example.B2C.modules.catalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock private ProductService productService;

    @InjectMocks private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/products - public access returns paged list")
    void searchProducts_publicAccess_returnsList() throws Exception {
        ProductSummaryDto summary = ProductSummaryDto.builder()
                .id(1L)
                .slug("iphone-15-pro")
                .name("iPhone 15 Pro")
                .minPrice(new BigDecimal("25000000"))
                .maxPrice(new BigDecimal("30000000"))
                .shopName("Test Shop")
                .categoryName("Electronics")
                .build();

        PageResponse<ProductSummaryDto> pageResponse = PageResponse.<ProductSummaryDto>builder()
                .content(List.of(summary))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(productService.searchProducts(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/products")
                        .param("categoryId", "10")
                        .param("q", "iphone")
                        .param("sortBy", "price")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].slug").value("iphone-15-pro"))
                .andExpect(jsonPath("$.data.content[0].shopName").value("Test Shop"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/products/featured - returns featured list")
    void getFeaturedProducts_returnsList() throws Exception {
        ProductSummaryDto summary = ProductSummaryDto.builder()
                .id(1L).slug("top-product").name("Top Product")
                .build();

        PageResponse<ProductSummaryDto> pageResponse = PageResponse.<ProductSummaryDto>builder()
                .content(List.of(summary))
                .page(0).size(20).totalElements(1L).totalPages(1)
                .first(true).last(true)
                .build();

        when(productService.getFeaturedProducts(0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].slug").value("top-product"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{slug} - existing slug returns detail")
    void getProductDetail_existingSlug_returnsDetail() throws Exception {
        ProductDetailDto detail = ProductDetailDto.builder()
                .id(1L)
                .slug("iphone-15-pro")
                .name("iPhone 15 Pro")
                .description("Latest Apple phone")
                .minPrice(new BigDecimal("25000000"))
                .maxPrice(new BigDecimal("30000000"))
                .shopName("Test Shop")
                .status("ACTIVE")
                .build();

        when(productService.getProductDetail("iphone-15-pro")).thenReturn(detail);

        mockMvc.perform(get("/api/v1/products/iphone-15-pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.slug").value("iphone-15-pro"))
                .andExpect(jsonPath("$.data.shopName").value("Test Shop"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{slug} - not found returns 404 via GlobalExceptionHandler")
    void getProductDetail_notFound_returns404() throws Exception {
        when(productService.getProductDetail("unknown"))
                .thenThrow(new ResourceNotFoundException("Product", "slug: unknown"));

        mockMvc.perform(get("/api/v1/products/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: slug: unknown"));
    }

    @Test
    @DisplayName("GET /api/v1/products/seller/{sellerId} - returns seller's products")
    void getProductsBySeller_returnsList() throws Exception {
        ProductSummaryDto summary = ProductSummaryDto.builder()
                .id(1L).slug("seller-product").name("Seller Product")
                .sellerId(5L).shopName("Shop X")
                .build();

        PageResponse<ProductSummaryDto> pageResponse = PageResponse.<ProductSummaryDto>builder()
                .content(List.of(summary))
                .page(0).size(20).totalElements(1L).totalPages(1)
                .first(true).last(true)
                .build();

        when(productService.getProductsBySeller(5L, 0, 20)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/products/seller/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].sellerId").value(5))
                .andExpect(jsonPath("$.data.content[0].shopName").value("Shop X"));
    }
}
