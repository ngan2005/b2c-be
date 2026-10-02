package com.example.B2C.common.exception;

public class OutOfStockException extends RuntimeException {

    public OutOfStockException(String message) {
        super(message);
    }
}
