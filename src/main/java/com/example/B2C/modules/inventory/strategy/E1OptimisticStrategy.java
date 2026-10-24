package com.example.B2C.modules.inventory.strategy;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * E1 — Optimistic locking via {@code @Version}. Concurrent updates that read the
 * same version will cause one transaction to fail with
 * {@link org.springframework.orm.ObjectOptimisticLockingFailureException}. The
 * caller may retry (E3) or surface the failure to the user.
 */
@Component
@RequiredArgsConstructor
public class E1OptimisticStrategy implements InventoryStrategy {

    private final ProductVariantRepository repository;

    @Override
    public String id() {
        return "E1";
    }

    @Override
    public ProductVariantRepository repository() {
        return repository;
    }

    @Override
    public void reserve(Long variantId, int qty, Long orderId) {
        ProductVariant v = repository.findById(variantId)
                .orElseThrow(() -> new OutOfStockException("Variant " + variantId + " not found"));
        if (v.getStockQuantity() < qty) {
            throw new OutOfStockException("Insufficient stock for variant " + variantId);
        }
        v.setStockQuantity(v.getStockQuantity() - qty);
        v.setReservedQuantity(
                (v.getReservedQuantity() == null ? 0 : v.getReservedQuantity()) + qty);
        v.setLastReservedAt(LocalDateTime.now());
        repository.saveAndFlush(v);
    }
}
