package com.example.B2C.common.exception;

public enum ErrorCode {

    SELLER_APPLICATION_NOT_FOUND("Seller application not found", 404),
    SELLER_APPLICATION_ALREADY_PENDING("A pending seller application already exists for this user", 409),
    SELLER_APPLICATION_ALREADY_APPROVED("Seller application has already been approved", 409),
    SELLER_APPLICATION_ALREADY_REJECTED("Seller application has already been rejected", 409),
    USER_ALREADY_SELLER("User already has a seller profile", 409),
    INVALID_SELLER_APPLICATION_STATUS("Invalid seller application status for this operation", 400),
    REJECTION_REASON_REQUIRED("Rejection reason is required when rejecting an application", 400),
    BUYER_ROLE_REQUIRED("Only a user with the BUYER role can submit a seller application", 403);

    private final String message;
    private final int httpStatus;

    ErrorCode(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
