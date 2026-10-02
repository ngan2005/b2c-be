package com.example.B2C.modules.cart.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.cart.dto.AddCartItemRequest;
import com.example.B2C.modules.cart.dto.CartItemDto;
import com.example.B2C.modules.cart.dto.UpdateCartItemRequest;
import com.example.B2C.modules.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Buyer shopping cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @Operation(summary = "List my cart items")
    public ResponseEntity<ApiResponse<List<CartItemDto>>> list() {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(cartService.listItems(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the cart")
    public ResponseEntity<ApiResponse<CartItemDto>> add(@Valid @RequestBody AddCartItemRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Item added", cartService.addItem(userId, request)));
    }

    @PatchMapping("/items/{id}")
    @Operation(summary = "Update quantity or selection of a cart item")
    public ResponseEntity<ApiResponse<CartItemDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Item updated", cartService.updateItem(userId, id, request)));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Remove a cart item")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        cartService.removeItem(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Item removed", null));
    }
}
