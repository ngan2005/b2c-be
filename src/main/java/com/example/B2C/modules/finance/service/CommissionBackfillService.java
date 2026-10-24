package com.example.B2C.modules.finance.service;

import com.example.B2C.modules.finance.entity.CommissionRecord;
import com.example.B2C.modules.finance.repository.CommissionRecordRepository;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.entity.OrderItem;
import com.example.B2C.modules.order.entity.OrderStatus;
import com.example.B2C.modules.order.repository.OrderItemRepository;
import com.example.B2C.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for backfilling commission records for existing COMPLETED orders.
 * Idempotent — relies on the UNIQUE constraint on commission_record.order_item_id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionBackfillService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CommissionRecordRepository commissionRecordRepository;
    private final CommissionService commissionService;

    /**
     * Backfill commission records for all COMPLETED orders that don't have any
     * commission_record yet for their items. Idempotent because of the
     * UNIQUE constraint on order_item_id in commission_record.
     *
     * @return the number of commission records created
     */
    @Transactional
    public int backfillAllCompletedOrders() {
        // Use findAllAdmin with status filter (it maps to WHERE status = :status when non-null)
        List<Order> completedOrders = orderRepository
                .findAllAdmin(OrderStatus.COMPLETED, org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        if (completedOrders.isEmpty()) {
            log.info("No COMPLETED orders found for backfill");
            return 0;
        }

        List<Long> orderIds = completedOrders.stream().map(Order::getId).toList();

        // Find which orders already have commission records
        List<Long> alreadyProcessed = commissionRecordRepository.findOrderIdsWithRecords(orderIds);
        List<Long> ordersNeedingBackfill = orderIds.stream()
                .filter(id -> !alreadyProcessed.contains(id))
                .toList();

        if (ordersNeedingBackfill.isEmpty()) {
            log.info("All {} COMPLETED orders already have commission records", orderIds.size());
            return 0;
        }

        int totalCreated = 0;
        for (Long orderId : ordersNeedingBackfill) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) continue;

            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : items) {
                if (commissionRecordRepository.existsByOrderItemId(item.getId())) {
                    continue; // double-check with in-memory filter too
                }
                try {
                    CommissionRecord rec = commissionService.snapshotForItem(item, order);
                    if (rec != null) {
                        totalCreated++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to backfill commission for order_item_id={}: {}",
                            item.getId(), e.getMessage());
                }
            }
        }

        log.info("Backfill complete: {} commission records created for {} orders",
                totalCreated, ordersNeedingBackfill.size());
        return totalCreated;
    }
}
