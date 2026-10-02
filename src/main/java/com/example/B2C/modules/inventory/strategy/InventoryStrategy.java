package com.example.B2C.modules.inventory.strategy;

import com.example.B2C.modules.catalog.entity.ProductVariant;

public interface InventoryStrategy {

    /**
     * Identifier of the strategy. One of {@code E0}, {@code E1}, {@code E2}, {@code E3}.
     */
    String id();

    /**
     * Reserve {@code qty} units of {@code variant} for {@code orderId}.
     *
     * <p>Implementations must:
     * <ul>
     *   <li>Persist changes to {@code variant} so that stockQuantity / reservedQuantity
     *       are visible to other transactions.</li>
     *   <li>Throw {@link com.example.B2C.common.exception.OutOfStockException} when
     *       the variant has insufficient stock.</li>
     *   <li>For optimistic strategies, throw {@link org.springframework.orm.ObjectOptimisticLockingFailureException}
     *       when a version conflict is detected so that the retry strategy can handle it.</li>
     * </ul>
     */
    void reserve(ProductVariant variant, int qty, Long orderId);
}
