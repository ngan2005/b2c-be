package com.example.B2C.modules.inventory.service;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.port.InventoryPort;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.strategy.InventoryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements InventoryPort {

    private final ProductVariantRepository repository;
    private final InventoryStrategy activeStrategy;

    @Override
    @Transactional(readOnly = true)
    public int getAvailableStock(Long variantId) {
        ProductVariant v = repository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        return Math.max(0, v.getStockQuantity() - reserved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(Long variantId, int qty) {
        return getAvailableStock(variantId) >= qty;
    }

    @Override
    @Transactional
    public void reserve(Long variantId, int qty, Long orderId) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        ProductVariant variant = repository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        log.debug("Reserving variant={} qty={} orderId={} strategy={}",
                variantId, qty, orderId, activeStrategy.id());
        activeStrategy.reserve(variant, qty, orderId);
    }

    @Override
    @Transactional
    public void release(Long variantId, int qty) {
        if (qty <= 0) {
            return;
        }
        ProductVariant v = repository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        int toRelease = Math.min(qty, reserved);
        v.setReservedQuantity(reserved - toRelease);
        v.setStockQuantity(v.getStockQuantity() + toRelease);
        repository.save(v);
        log.info("Released {} units back to stock for variant {}", toRelease, variantId);
    }

    @Override
    @Transactional
    public void confirm(Long variantId, int qty) {
        if (qty <= 0) {
            return;
        }
        ProductVariant v = repository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        int toConfirm = Math.min(qty, reserved);
        v.setReservedQuantity(reserved - toConfirm);
        v.setSoldCount((v.getSoldCount() == null ? 0 : v.getSoldCount()) + toConfirm);
        repository.save(v);
        log.info("Confirmed {} units as sold for variant {}", toConfirm, variantId);
    }

    /**
     * Convenience: derive the current strategy id for logging/metrics.
     */
    public String currentStrategyId() {
        return activeStrategy.id();
    }

    /**
     * Used by load tests only — wraps reserve and rethrows as OutOfStockException so
     * that contention failures are normalised across strategies.
     */
    public void reserveForLoadTest(Long variantId, int qty) {
        try {
            reserve(variantId, qty, null);
        } catch (org.springframework.dao.OptimisticLockingFailureException ex) {
            throw new OutOfStockException("Optimistic lock conflict on variant " + variantId);
        }
    }
}
