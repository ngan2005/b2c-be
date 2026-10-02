package com.example.B2C.modules.inventory.strategy;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * E0 — No locking. Reads the variant without any lock, decrements stock, saves.
 * Intended as a baseline that demonstrates the oversell race condition under
 * concurrent load. Do not use in production.
 */
@Component
@RequiredArgsConstructor
public class E0NoLockStrategy implements InventoryStrategy {

    private final ProductVariantRepository repository;

    @Override
    public String id() {
        return "E0";
    }

    @Override
    public void reserve(ProductVariant variant, int qty, Long orderId) {
        if (variant.getStockQuantity() < qty) {
            throw new OutOfStockException("Insufficient stock for variant " + variant.getId());
        }
        variant.setStockQuantity(variant.getStockQuantity() - qty);
        variant.setReservedQuantity(
                (variant.getReservedQuantity() == null ? 0 : variant.getReservedQuantity()) + qty);
        variant.setLastReservedAt(LocalDateTime.now());
        repository.save(variant);
    }
}
