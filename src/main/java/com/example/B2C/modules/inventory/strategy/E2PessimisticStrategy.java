package com.example.B2C.modules.inventory.strategy;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * E2 — Pessimistic locking. Acquires {@code SELECT ... FOR UPDATE} on the variant
 * row so that concurrent transactions block until the first one commits.
 */
@Component
@RequiredArgsConstructor
public class E2PessimisticStrategy implements InventoryStrategy {

    private final ProductVariantRepository repository;

    @Override
    public String id() {
        return "E2";
    }

    @Override
    public ProductVariantRepository repository() {
        return repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void reserve(Long variantId, int qty, Long orderId) {
        ProductVariant locked = repository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new OutOfStockException("Variant " + variantId + " not found"));
        if (locked.getStockQuantity() < qty) {
            throw new OutOfStockException("Insufficient stock for variant " + variantId);
        }
        locked.setStockQuantity(locked.getStockQuantity() - qty);
        locked.setReservedQuantity(
                (locked.getReservedQuantity() == null ? 0 : locked.getReservedQuantity()) + qty);
        locked.setLastReservedAt(LocalDateTime.now());
        repository.save(locked);
    }
}
