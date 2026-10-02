package com.example.B2C.modules.inventory.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.modules.inventory.entity.LoadTestResult;
import com.example.B2C.modules.inventory.repository.LoadTestResultRepository;
import com.example.B2C.modules.inventory.service.LoadTestRunner;
import com.example.B2C.modules.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/variants/{id}/stock")
    @Operation(summary = "Get current available stock for a variant")
    public ResponseEntity<ApiResponse<Integer>> stock(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAvailableStock(id)));
    }

    @PostMapping("/load-test/run")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Run a concurrency experiment for a given scenario and variant")
    public ResponseEntity<ApiResponse<LoadTestResult>> run(
            @RequestParam String scenario,
            @RequestParam Long variantId,
            @RequestParam(defaultValue = "20") int concurrency,
            @RequestParam(defaultValue = "5") int perThread) {
        LoadTestResult result = loadTestRunner.runExperiment(scenario, variantId, concurrency, perThread);
        return ResponseEntity.ok(ApiResponse.success("Experiment completed", result));
    }

    @GetMapping("/load-test/results")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all load test results")
    public ResponseEntity<ApiResponse<List<LoadTestResult>>> listResults() {
        return ResponseEntity.ok(ApiResponse.success(resultRepository.findAll()));
    }
}
