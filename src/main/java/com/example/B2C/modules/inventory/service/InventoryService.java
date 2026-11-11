package com.example.B2C.modules.inventory.service;

import com.example.B2C.common.exception.OutOfStockException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.port.InventoryPort;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.inventory.strategy.InventoryStrategy;
import com.example.B2C.modules.order.repository.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional(readOnly = true)
    public int getAvailableStock(Long variantId) {
        ProductVariant v = repository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        int stock = v.getStockQuantity() == null ? 0 : v.getStockQuantity();
        return Math.max(0, stock - reserved);
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
    @Override
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
    @Override
    @Transactional
    public void releaseSold(Long variantId, int qty) {
        if (qty <= 0) {
            return;
        }
        ProductVariant v = repository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        releaseSoldInternal(v, qty);
    }

    @Override
    @Transactional
    public int reconcileReservations(Long variantId) {
        ProductVariant v = repository.findByIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        int actualActive = orderItemRepository.sumActiveReservationsByVariantId(variantId);
        int currentReserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        if (currentReserved != actualActive) {
            log.info("Reconciled reservations for variant {}: {} -> {}", variantId, currentReserved, actualActive);
            v.setReservedQuantity(actualActive);
            repository.save(v);
        }
        return actualActive;
    }

    private void releaseReservedInternal(ProductVariant v, int qty) {
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        int toRelease = Math.min(qty, reserved);
        if (reserved < qty) {
            log.warn("Variant {} requested release of {} reserved units, but only {} were reserved. Clamping.",
                    v.getId(), qty, reserved);
        }
        v.setReservedQuantity(reserved - toRelease);
        repository.save(v);
        log.info("Released {} reserved units for variant {}", toRelease, v.getId());
    }

    private void releaseSoldInternal(ProductVariant v, int qty) {
        int sold = v.getSoldCount() == null ? 0 : v.getSoldCount();
        int toDeduct = Math.min(qty, sold);
        if (sold < qty) {
            log.warn("Variant {} requested return of {} sold units, but only {} recorded sold. Clamping soldCount.",
                    v.getId(), qty, sold);
        }
        v.setSoldCount(sold - toDeduct);
        v.setStockQuantity((v.getStockQuantity() == null ? 0 : v.getStockQuantity()) + qty);
        repository.save(v);
        log.info("Released {} returned units back to physical stock for variant {}", qty, v.getId());
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void confirm(Long variantId, int qty) {
        if (variantId == null || qty <= 0) {
            return;
        }
        ProductVariant v = repository.findByIdForUpdate(variantId)
                .orElse(null);
        if (v == null) {
            log.warn("Variant {} not found when confirming stock", variantId);
            return;
        }
        int reserved = v.getReservedQuantity() == null ? 0 : v.getReservedQuantity();
        int toConfirm = Math.min(qty, reserved);
        v.setReservedQuantity(reserved - toConfirm);
        v.setStockQuantity(Math.max(0, (v.getStockQuantity() == null ? 0 : v.getStockQuantity()) - toConfirm));
        v.setSoldCount((v.getSoldCount() == null ? 0 : v.getSoldCount()) + toConfirm);
        repository.save(v);
        log.info("Confirmed {} units as sold for variant {}", toConfirm, variantId);
    }

    @Transactional
    public void resetStockForExperiment(Long variantId, int stock) {
        ProductVariant v = repository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + variantId));
        v.setStockQuantity(Math.max(0, stock));
        v.setReservedQuantity(0);
        v.setSoldCount(0);
        repository.save(v);
        log.info("Reset stock for variant {} to {} (reserved=0)", variantId, stock);
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
