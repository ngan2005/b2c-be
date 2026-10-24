package com.example.B2C.modules.dispute.entity;

/**
 * Reason category chosen by the buyer when opening a dispute.
 */
public enum DisputeType {
    NOT_RECEIVED,  // did not receive the item
    WRONG_ITEM,    // received wrong/different item
    DAMAGED,       // item arrived damaged
    OTHER          // anything else
}
