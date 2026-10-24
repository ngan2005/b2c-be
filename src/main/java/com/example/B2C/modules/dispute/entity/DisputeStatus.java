package com.example.B2C.modules.dispute.entity;

/**
 * Status lifecycle for an order dispute.
 */
public enum DisputeStatus {
    OPEN,                // buyer just opened
    INVESTIGATING,       // admin started looking at it
    RESOLVED_REFUND,     // full refund granted
    RESOLVED_REJECT,     // dispute rejected
    RESOLVED_PARTIAL,    // partial refund
    CLOSED               // manually closed without resolution
}
