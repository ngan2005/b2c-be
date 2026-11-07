package com.example.B2C.modules.inventory.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.dto.LoadTestExecutionResponse;
import com.example.B2C.modules.inventory.dto.SelectableProductVariantDto;
import com.example.B2C.modules.inventory.entity.LoadTestResult;
import com.example.B2C.modules.inventory.repository.LoadTestResultRepository;
import com.example.B2C.modules.inventory.service.LoadTestRunner;
import com.example.B2C.modules.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory operations and concurrency experiment runner")
public class InventoryController {

    private final InventoryService inventoryService;
    private final LoadTestRunner loadTestRunner;
    private final LoadTestResultRepository resultRepository;
    private final ProductVariantRepository variantRepository;

    @GetMapping("/variants/{id}/stock")
    @Operation(summary = "Get current available stock for a variant")
    public ResponseEntity<ApiResponse<Integer>> stock(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAvailableStock(id)));
    }

    @GetMapping("/real-products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get list of real products for interactive admin testing")
    public ResponseEntity<ApiResponse<List<SelectableProductVariantDto>>> getRealProducts() {
        List<ProductVariant> variants = variantRepository.findTopVariantsForAdmin(PageRequest.of(0, 50));
        List<SelectableProductVariantDto> dtos = variants.stream().map(v -> {
            int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
            if (reserved > 0) {
                try {
                    reserved = inventoryService.reconcileReservations(v.getId());
                } catch (Exception ignored) {}
            }
            int stock = v.getStockQuantity() == null ? 0 : v.getStockQuantity();
            int available = Math.max(0, stock - reserved);
            String img = v.getImageUrl();
            if ((img == null || img.isBlank()) && v.getProduct() != null) {
                img = v.getProduct().getThumbnailUrl();
            }
            String shop = (v.getProduct() != null && v.getProduct().getSeller() != null)
                    ? v.getProduct().getSeller().getShopName() : "Sàn NoMall";
            return SelectableProductVariantDto.builder()
                    .variantId(v.getId())
                    .productId(v.getProduct() != null ? v.getProduct().getId() : null)
                    .productName(v.getProduct() != null ? v.getProduct().getName() : "—")
                    .variantName(v.getVariantName())
                    .sku(v.getSku())
                    .imageUrl(img)
                    .price(v.getSalePrice() != null ? v.getSalePrice() : v.getPrice())
                    .stockQuantity(stock)
                    .reservedQuantity(reserved)
                    .availableStock(available)
                    .shopName(shop)
                    .build();
        }).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PutMapping("/variants/{id}/reconcile")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reconcile reserved quantity against active orders")
    public ResponseEntity<ApiResponse<Integer>> reconcileStock(@PathVariable Long id) {
        int reconciled = inventoryService.reconcileReservations(id);
        return ResponseEntity.ok(ApiResponse.success("Đã đồng bộ lại lượng giữ chỗ thực tế", reconciled));
    }

    @PutMapping("/variants/{id}/reset-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset available stock and clear reservations for experiment")
    public ResponseEntity<ApiResponse<Integer>> resetStock(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int stockQuantity) {
        inventoryService.resetStockForExperiment(id, stockQuantity);
        return ResponseEntity.ok(ApiResponse.success("Đã cài lại tồn kho", stockQuantity));
    }

    @PostMapping("/load-test/run")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Run a concurrency experiment for a given scenario and variant")
    public ResponseEntity<ApiResponse<LoadTestExecutionResponse>> run(
            @RequestParam String scenario,
            @RequestParam Long variantId,
            @RequestParam(defaultValue = "20") int concurrency,
            @RequestParam(defaultValue = "5") int perThread) {
        LoadTestExecutionResponse response = loadTestRunner.runExperiment(scenario, variantId, concurrency, perThread);
        return ResponseEntity.ok(ApiResponse.success("Experiment completed", response));
    }

    @GetMapping("/load-test/results")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all load test results")
    public ResponseEntity<ApiResponse<List<LoadTestResult>>> listResults() {
        return ResponseEntity.ok(ApiResponse.success(resultRepository.findAll()));
    }
}
