package com.example.B2C.common.event;

import lombok.Getter;

@Getter
public class OrderCancelledEvent extends DomainEvent {

    private final Long orderId;
    private final Long buyerId;
    private final Long sellerId;
    private final String reason;

    public OrderCancelledEvent(Object source, Long orderId, Long buyerId, Long sellerId, String reason) {
        super(source);
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.reason = reason;
    }
}
