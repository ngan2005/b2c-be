package com.example.B2C.modules.inventory.strategy;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * E3 — Optimistic locking with bounded retry. Re-runs the optimistic reserve up to
 * {@code inventory.locking.retry.max-attempts} times with a small backoff before
 * giving up.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class E3OptimisticRetryStrategy implements InventoryStrategy {

    private final ProductVariantRepository repository;

    @Value("${inventory.locking.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${inventory.locking.retry.backoff-ms:50}")
    private long backoffMs;

    @Override
    public String id() {
        return "E3";
    }

    @Override
    public void reserve(ProductVariant variant, int qty, Long orderId) {
        ObjectOptimisticLockingFailureException lastEx = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ProductVariant fresh = repository.findById(variant.getId())
                        .orElseThrow(() -> new OutOfStockException(
                                "Variant " + variant.getId() + " not found"));
                if (fresh.getStockQuantity() < qty) {
                    throw new OutOfStockException("Insufficient stock for variant " + fresh.getId());
                }
                fresh.setStockQuantity(fresh.getStockQuantity() - qty);
                fresh.setReservedQuantity(
                        (fresh.getReservedQuantity() == null ? 0 : fresh.getReservedQuantity()) + qty);
                fresh.setLastReservedAt(LocalDateTime.now());
                repository.saveAndFlush(fresh);
                return;
            } catch (ObjectOptimisticLockingFailureException ex) {
                lastEx = ex;
                log.debug("Optimistic conflict on variant {} attempt {}/{}",
                        variant.getId(), attempt, maxAttempts);
                if (attempt < maxAttempts) {
                    sleep(backoffMs * attempt);
                }
            }
        }
        throw lastEx != null ? lastEx
                : new OutOfStockException("Failed to reserve variant " + variant.getId());
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
