package com.example.B2C.modules.inventory.strategy;

import com.example.B2C.modules.catalog.repository.ProductVariantRepository;

/**
 * Per-strategy reservation algorithm. Implementations must:
 * <ul>
 *   <li>Re-read the variant row with the strategy's own locking semantics so the
 *       read is repeatable for the duration of the reservation.</li>
 *   <li>Persist changes to the variant so that {@code stockQuantity} /
 *       {@code reservedQuantity} are visible to other transactions.</li>
 *   <li>Throw {@link com.example.B2C.common.exception.OutOfStockException} when the
 *       variant has insufficient stock.</li>
 *   <li>For optimistic strategies, throw
 *       {@link org.springframework.orm.ObjectOptimisticLockingFailureException} when
 *       a version conflict is detected so that the retry strategy can handle it.</li>
 * </ul>
 */
public interface InventoryStrategy {

    /**
     * Identifier of the strategy. One of {@code E0}, {@code E1}, {@code E2}, {@code E3}.
     */
    String id();

    /**
     * Reserve {@code qty} units of the variant identified by {@code variantId} for
     * {@code orderId}. Each implementation is responsible for its own read with the
     * appropriate locking semantics.
     */
    void reserve(Long variantId, int qty, Long orderId);

    /**
     * The repository used to load the variant. Exposed for strategies that need to
     * re-read the row (E1, E2, E3).
     */
    ProductVariantRepository repository();
}
