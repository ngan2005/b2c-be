package com.example.B2C.modules.inventory;

import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.entity.LoadTestResult;
import com.example.B2C.modules.inventory.repository.LoadTestResultRepository;
import com.example.B2C.modules.inventory.service.InventoryService;
import com.example.B2C.modules.inventory.service.LoadTestRunner;
import com.example.B2C.modules.inventory.strategy.E0NoLockStrategy;
import com.example.B2C.modules.inventory.strategy.E1OptimisticStrategy;
import com.example.B2C.modules.inventory.strategy.E2PessimisticStrategy;
import com.example.B2C.modules.inventory.strategy.E3OptimisticRetryStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pure unit test for LoadTestRunner: mock all collaborators, exercise the
 * scenario mapping, and assert that the persisted LoadTestResult carries a
 * scenario value the V8 CHECK constraint can accept.
 *
 * <p>The bug we are pinning down: the Admin UI sends scenario values from a
 * hardcoded list ({@code PESSIMISTIC_WRITE}, {@code OPTIMISTIC}, {@code NONE}).
 * The DB schema (V8__load_test_result.sql) constrains the column to
 * {@code E0,E1,E2,E3}. LoadTestRunner passes the FE string through unchanged,
 * so a save attempt would violate {@code chk_load_test_scenario}.
 */
class LoadTestRunnerScenarioTest {

    private LoadTestRunner newRunner(ProductVariantRepository variantRepo,
                                     LoadTestResultRepository resultRepo,
                                     InventoryService inventory) {
        TransactionTemplate tx = Mockito.mock(TransactionTemplate.class);
        // The unit test doesn't actually exercise the lock; we just want to
        // see what gets persisted.
        Mockito.doNothing().when(tx).executeWithoutResult(any());
        org.springframework.jdbc.core.JdbcTemplate jdbc = Mockito.mock(org.springframework.jdbc.core.JdbcTemplate.class);
        E0NoLockStrategy e0 = Mockito.mock(E0NoLockStrategy.class);
        E1OptimisticStrategy e1 = Mockito.mock(E1OptimisticStrategy.class);
        E2PessimisticStrategy e2 = Mockito.mock(E2PessimisticStrategy.class);
        E3OptimisticRetryStrategy e3 = Mockito.mock(E3OptimisticRetryStrategy.class);
        return new LoadTestRunner(inventory, variantRepo, resultRepo, tx, jdbc, e0, e1, e2, e3);
    }

    @Test
    @DisplayName("FIXED: 'PESSIMISTIC_WRITE' is normalised to E2 before persist")
    void feScenarioIsPersistedAsE2() {
        var variant = new ProductVariant();
        variant.setId(1L);
        variant.setStockQuantity(50);
        variant.setReservedQuantity(0);
        variant.setSoldCount(0);

        ProductVariantRepository variantRepo = Mockito.mock(ProductVariantRepository.class);
        LoadTestResultRepository resultRepo = Mockito.mock(LoadTestResultRepository.class);
        InventoryService inventory = Mockito.mock(InventoryService.class);

        when(variantRepo.findById(1L)).thenReturn(Optional.of(variant));
        when(inventory.currentStrategyId()).thenReturn("E1");
        when(resultRepo.save(any(LoadTestResult.class))).thenAnswer(inv -> {
            LoadTestResult arg = inv.getArgument(0);
            arg.setId(42L);
            return arg;
        });

        LoadTestRunner runner = newRunner(variantRepo, resultRepo, inventory);
        runner.runExperiment("PESSIMISTIC_WRITE", 1L, 2, 1);

        ArgumentCaptor<LoadTestResult> captor = ArgumentCaptor.forClass(LoadTestResult.class);
        Mockito.verify(resultRepo).save(captor.capture());
        LoadTestResult persisted = captor.getValue();

        System.out.println(">>> Scenario persisted: '" + persisted.getScenario() + "'");
        assertEquals("E2", persisted.getScenario(),
                "PESSIMISTIC_WRITE must be normalised to E2");
    }

    @Test
    @DisplayName("FIXED: 'OPTIMISTIC' is normalised to E1")
    void optimisticMapsToE1() {
        var variant = new ProductVariant();
        variant.setId(3L);
        variant.setStockQuantity(50);
        variant.setReservedQuantity(0);
        variant.setSoldCount(0);

        ProductVariantRepository variantRepo = Mockito.mock(ProductVariantRepository.class);
        LoadTestResultRepository resultRepo = Mockito.mock(LoadTestResultRepository.class);
        InventoryService inventory = Mockito.mock(InventoryService.class);

        when(variantRepo.findById(3L)).thenReturn(Optional.of(variant));
        when(resultRepo.save(any(LoadTestResult.class))).thenAnswer(inv -> inv.getArgument(0));

        LoadTestRunner runner = newRunner(variantRepo, resultRepo, inventory);
        runner.runExperiment("OPTIMISTIC", 3L, 2, 1);

        ArgumentCaptor<LoadTestResult> captor = ArgumentCaptor.forClass(LoadTestResult.class);
        Mockito.verify(resultRepo).save(captor.capture());
        assertEquals("E1", captor.getValue().getScenario());
    }

    @Test
    @DisplayName("FIXED: 'NONE' is normalised to E0")
    void noneMapsToE0() {
        var variant = new ProductVariant();
        variant.setId(4L);
        variant.setStockQuantity(50);
        variant.setReservedQuantity(0);
        variant.setSoldCount(0);

        ProductVariantRepository variantRepo = Mockito.mock(ProductVariantRepository.class);
        LoadTestResultRepository resultRepo = Mockito.mock(LoadTestResultRepository.class);
        InventoryService inventory = Mockito.mock(InventoryService.class);

        when(variantRepo.findById(4L)).thenReturn(Optional.of(variant));
        when(resultRepo.save(any(LoadTestResult.class))).thenAnswer(inv -> inv.getArgument(0));

        LoadTestRunner runner = newRunner(variantRepo, resultRepo, inventory);
        runner.runExperiment("NONE", 4L, 2, 1);

        ArgumentCaptor<LoadTestResult> captor = ArgumentCaptor.forClass(LoadTestResult.class);
        Mockito.verify(resultRepo).save(captor.capture());
        assertEquals("E0", captor.getValue().getScenario());
    }

    @Test
    @DisplayName("E0-E3 pass through unchanged")
    void backendCodePersistsUnchanged() {
        var variant = new ProductVariant();
        variant.setId(2L);
        variant.setStockQuantity(50);
        variant.setReservedQuantity(0);
        variant.setSoldCount(0);

        ProductVariantRepository variantRepo = Mockito.mock(ProductVariantRepository.class);
        LoadTestResultRepository resultRepo = Mockito.mock(LoadTestResultRepository.class);
        InventoryService inventory = Mockito.mock(InventoryService.class);

        when(variantRepo.findById(2L)).thenReturn(Optional.of(variant));
        when(resultRepo.save(any(LoadTestResult.class))).thenAnswer(inv -> inv.getArgument(0));

        LoadTestRunner runner = newRunner(variantRepo, resultRepo, inventory);
        runner.runExperiment("E0", 2L, 2, 1);

        ArgumentCaptor<LoadTestResult> captor = ArgumentCaptor.forClass(LoadTestResult.class);
        Mockito.verify(resultRepo).save(captor.capture());
        assertEquals("E0", captor.getValue().getScenario());
    }

    @Test
    @DisplayName("Invalid scenario is rejected with IllegalArgumentException")
    void invalidScenarioIsRejected() {
        var variant = new ProductVariant();
        variant.setId(5L);
        variant.setStockQuantity(50);

        ProductVariantRepository variantRepo = Mockito.mock(ProductVariantRepository.class);
        LoadTestResultRepository resultRepo = Mockito.mock(LoadTestResultRepository.class);
        InventoryService inventory = Mockito.mock(InventoryService.class);
        when(variantRepo.findById(5L)).thenReturn(Optional.of(variant));

        LoadTestRunner runner = newRunner(variantRepo, resultRepo, inventory);
        assertThrows(IllegalArgumentException.class,
                () -> runner.runExperiment("BOGUS_VALUE", 5L, 1, 1));
    }
}
