package com.example.B2C.modules.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleCode {
    BUYER("BUYER"),
    SELLER("SELLER"),
    ADMIN("ADMIN");

    private final String value;

    RoleCode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RoleCode fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RoleCode rc : RoleCode.values()) {
            if (rc.value.equalsIgnoreCase(value)) {
                return rc;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + value);
    }
}
