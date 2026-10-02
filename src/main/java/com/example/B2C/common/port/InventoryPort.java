package com.example.B2C.common.port;

/**
 * Cross-module port for inventory operations. Implementations live in
 * {@code modules/inventory}. Cart and Order modules depend on this interface only,
 * never on the concrete repository/service of the inventory module.
 */
public interface InventoryPort {

    /**
     * @return available stock quantity for the given variant (after subtracting any
     *         currently reserved stock).
     */
    int getAvailableStock(Long variantId);

    /**
     * @return true if at least {@code qty} units are available right now.
     */
    boolean isAvailable(Long variantId, int qty);

    /**
     * Reserve {@code qty} units of a variant for the given order. Behaviour depends on
     * the active locking strategy (E0/E1/E2/E3).
     *
     * @throws com.example.B2C.common.exception.OutOfStockException if not enough stock
     */
    void reserve(Long variantId, int qty, Long orderId);

    /**
     * Release a previously reserved quantity (e.g. when an order is cancelled).
     */
    void release(Long variantId, int qty);

    /**
     * Convert reserved stock into sold stock. Called when payment is confirmed.
     */
    void confirm(Long variantId, int qty);
}
