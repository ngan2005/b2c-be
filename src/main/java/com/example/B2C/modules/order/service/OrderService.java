package com.example.B2C.modules.order.service;

import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.order.dto.CreateOrderRequest;
import com.example.B2C.modules.order.dto.OrderDetailDto;
import com.example.B2C.modules.order.dto.OrderSummaryDto;
import com.example.B2C.modules.order.entity.OrderStatus;

public interface OrderService {

    PageResponse<OrderSummaryDto> listOrdersForCurrentSeller(Long userId, OrderStatus status, int page, int size);

    OrderDetailDto getOrderForSeller(Long userId, Long orderId);

    OrderDetailDto updateOrderStatusForSeller(Long userId, Long orderId, OrderStatus status, String note);

    OrderDetailDto createOrderForBuyer(Long userId, CreateOrderRequest request, String idempotencyKey);

    PageResponse<OrderSummaryDto> listOrdersForCurrentBuyer(Long userId, OrderStatus status, int page, int size);

    OrderDetailDto getOrderForBuyer(Long userId, Long orderId);

    OrderDetailDto cancelOrderForBuyer(Long userId, Long orderId, String reason);
}