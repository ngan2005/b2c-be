package com.example.B2C.modules.order.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.order.dto.CreateOrderRequest;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderSummaryDto;
import com.example.B2C.modules.order.dto.ReturnRequestDto;
import com.example.B2C.modules.order.entity.OrderStatus;

public interface OrderService {

    PageResponse<OrderSummaryDto> listOrdersForCurrentSeller(Long userId, OrderStatus status, int page, int size);

    OrderDetailDto getOrderForSeller(Long userId, Long orderId);

    OrderDetailDto updateOrderStatusForSeller(Long userId, Long orderId, OrderStatus status, String note);

    OrderDetailDto createOrderForBuyer(Long userId, CreateOrderRequest request, String idempotencyKey);

    PageResponse<OrderSummaryDto> listOrdersForCurrentBuyer(Long userId, OrderStatus status, int page, int size);

    /**
     * Read-only list of orders where the user is the buyer_id.
     * Used by the buyer-orders-history endpoint which is open to both BUYER
     * and SELLER (the latter can see orders they placed BEFORE upgrading).
     */
    PageResponse<OrderSummaryDto> listHistoricalBuyerOrders(Long userId, OrderStatus status, int page, int size);

    OrderDetailDto getOrderForBuyer(Long userId, Long orderId);

    OrderDetailDto cancelOrderForBuyer(Long userId, Long orderId, String reason);

    OrderDetailDto requestReturn(Long userId, Long orderId, ReturnRequestDto request);

    OrderDetailDto approveReturn(Long orderId);

    OrderDetailDto rejectReturn(Long orderId, String adminNote);

    PageResponse<OrderSummaryDto> listOrdersForAdmin(OrderStatus status, int page, int size);

    OrderDetailDto getOrderForAdmin(Long orderId);
}
