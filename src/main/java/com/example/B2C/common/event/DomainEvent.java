package com.example.B2C.common.event;

import org.springframework.context.ApplicationEvent;

public abstract class DomainEvent extends ApplicationEvent {
    protected DomainEvent(Object source) {
        super(source);
    }
}
