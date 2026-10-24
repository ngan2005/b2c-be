package com.example.B2C.modules.finance.entity;

/**
 * Status of a seller payout record.
 */
public enum PayoutStatus {
    /** Awaiting admin approval / processing */
    PENDING,
    /** Approved and ready to be paid out */
    READY,
    /** Bank transfer confirmed by admin */
    PAID,
    /** Cancelled by admin (only from PENDING) */
    CANCELLED
}
