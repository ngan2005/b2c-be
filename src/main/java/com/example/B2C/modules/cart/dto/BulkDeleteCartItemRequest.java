package com.example.B2C.modules.cart.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkDeleteCartItemRequest {
    @NotEmpty(message = "cartItemIds must not be empty")
    private List<Long> cartItemIds;
}

