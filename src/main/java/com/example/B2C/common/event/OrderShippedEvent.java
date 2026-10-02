package com.example.B2C.common.event;

import lombok.Getter;

@Getter
public class OrderShippedEvent extends DomainEvent {

    private final Long orderId;
    private final Long buyerId;
    private final Long sellerId;

    public OrderShippedEvent(Object source, Long orderId, Long buyerId, Long sellerId) {
        super(source);
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
    }
}
