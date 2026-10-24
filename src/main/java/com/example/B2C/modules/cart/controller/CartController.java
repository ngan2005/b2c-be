package com.example.B2C.modules.cart.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.cart.dto.AddCartItemRequest;
import com.example.B2C.modules.cart.dto.CartItemDto;
import com.example.B2C.modules.cart.dto.UpdateCartItemRequest;
import com.example.B2C.modules.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Buyer - Cart", description = "Authenticated buyer's shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @Operation(summary = "List my cart items")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart items returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<ApiResponse<List<CartItemDto>>> list() {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(cartService.listItems(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the cart")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item added to cart"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid quantity, inactive variant, or insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Variant not found")
    })
    public ResponseEntity<ApiResponse<CartItemDto>> add(@Valid @RequestBody AddCartItemRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Item added", cartService.addItem(userId, request)));
    }

    @PatchMapping("/items/{id}")
    @Operation(summary = "Update quantity or selection of a cart item")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<ApiResponse<CartItemDto>> update(
            @Parameter(description = "Cart item ID") @PathVariable Long id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Item updated", cartService.updateItem(userId, id, request)));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Remove a cart item")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<ApiResponse<Void>> remove(@Parameter(description = "Cart item ID") @PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        cartService.removeItem(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Item removed", null));
    }
}
