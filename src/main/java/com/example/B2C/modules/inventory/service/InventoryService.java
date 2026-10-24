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

/**
 * Single owner of the inventory invariant:
 * <pre>
 *   stockQuantity       = physical units in the warehouse
 *   reservedQuantity    = units soft-held by PENDING / unpaid orders
 *   soldCount           = units that left the warehouse (paid and not returned)
 *   stockQuantity + reservedQuantity + soldCount = initial physical stock (per variant)
 * </pre>
 *
 * Every public mutator in this class preserves the invariant. Callers must use the
 * right method for the lifecycle stage:
 * <ul>
 *   <li>{@link #reserve} — when the order is created, before payment.</li>
 *   <li>{@link #confirm} — when payment is confirmed (reserved -&gt; sold).</li>
 *   <li>{@link #release} — when an order is cancelled (before or after payment);
 *       the implementation dispatches to {@code releaseReserved} or
 *       {@code releaseSold} based on the current row state.</li>
 * </ul>
 */
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
        if (!repository.existsById(variantId)) {
            throw new ResourceNotFoundException("Variant", "id: " + variantId);
        }
        log.debug("Reserving variant={} qty={} orderId={} strategy={}",
                variantId, qty, orderId, activeStrategy.id());
        // Each strategy is responsible for re-reading the row with its own locking
        // semantics, then mutating and persisting. We no longer pre-read here.
        activeStrategy.reserve(variantId, qty, orderId);
    }

    @Override
    @Transactional
    public void release(Long variantId, int qty) {
        if (qty <= 0) {
            return;
        }
        // Dispatch based on current row state: prefer releasing from reserved (the
        // common case: cancel before payment). If the order was already confirmed,
        // the units are in soldCount — release from there instead so stock does not
        // inflate.
        ProductVariant v = repository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        int sold = v.getSoldCount() == null ? 0 : v.getSoldCount();

        if (reserved >= qty) {
            releaseReservedInternal(v, qty);
        } else if (sold >= qty) {
            releaseSoldInternal(v, qty);
        } else {
            throw new IllegalStateException(
                    "Cannot release " + qty + " units of variant " + variantId
                            + ": reserved=" + reserved + " sold=" + sold);
        }
    }

    /**
     * Release stock that was held in {@code reservedQuantity} (cancel before payment).
     * Invariant: {@code stockQuantity += qty; reservedQuantity -= qty}.
     */
    @Transactional
    public void releaseReserved(Long variantId, int qty) {
        if (qty <= 0) {
            return;
        }
        ProductVariant v = repository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        releaseReservedInternal(v, qty);
    }

    /**
     * Release stock that was already confirmed (cancel after payment, e.g. admin
     * override / refund). Invariant: {@code stockQuantity += qty; soldCount -= qty}.
     */
    @Transactional
    public void releaseSold(Long variantId, int qty) {
        if (qty <= 0) {
            return;
        }
        ProductVariant v = repository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        releaseSoldInternal(v, qty);
    }

    private void releaseReservedInternal(ProductVariant v, int qty) {
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        if (reserved < qty) {
            throw new IllegalStateException(
                    "Cannot release " + qty + " reserved units of variant " + v.getId()
                            + ": only " + reserved + " reserved");
        }
        v.setReservedQuantity(reserved - qty);
        v.setStockQuantity(v.getStockQuantity() + qty);
        repository.save(v);
        log.info("Released {} reserved units back to stock for variant {}", qty, v.getId());
    }

    private void releaseSoldInternal(ProductVariant v, int qty) {
        int sold = v.getSoldCount() == null ? 0 : v.getSoldCount();
        if (sold < qty) {
            throw new IllegalStateException(
                    "Cannot release " + qty + " sold units of variant " + v.getId()
                            + ": only " + sold + " sold");
        }
        v.setSoldCount(sold - qty);
        v.setStockQuantity(v.getStockQuantity() + qty);
        repository.save(v);
        log.info("Released {} sold units back to stock for variant {}", qty, v.getId());
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
