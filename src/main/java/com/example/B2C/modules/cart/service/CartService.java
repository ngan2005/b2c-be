package com.example.B2C.modules.cart.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.port.InventoryPort;
import com.example.B2C.modules.cart.dto.AddCartItemRequest;
import com.example.B2C.modules.cart.dto.CartItemDto;
import com.example.B2C.modules.cart.dto.UpdateCartItemRequest;
import com.example.B2C.modules.cart.entity.Cart;
import com.example.B2C.modules.cart.entity.CartItem;
import com.example.B2C.modules.cart.repository.CartItemRepository;
import com.example.B2C.modules.cart.repository.CartRepository;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import com.example.B2C.modules.catalog.repository.ProductVariantRepository;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryPort inventoryPort;

    public List<CartItemDto> listItems(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.findByCartId(cart.getId())
                .stream()
                .map(CartService::toDto)
                .toList();
    }

    @Transactional
    public CartItemDto addItem(Long userId, AddCartItemRequest req) {
        if (req.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be > 0");
        }
        ProductVariant variant = variantRepository.findById(req.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id: " + req.getVariantId()));
        if (Boolean.FALSE.equals(variant.getIsActive())) {
            throw new BadRequestException("Variant is not active");
        }
        if (!inventoryPort.isAvailable(variant.getId(), req.getQuantity())) {
            throw new BadRequestException("Requested quantity not available");
        }
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId())
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .variant(variant)
                        .priceSnapshot(snapshotPrice(variant))
                        .quantity(0)
                        .isSelected(true)
                        .build());
        item.setQuantity(item.getQuantity() + req.getQuantity());
        item.setPriceSnapshot(snapshotPrice(variant));
        CartItem saved = cartItemRepository.save(item);
        return toDto(saved);
    }

    @Transactional
    public CartItemDto updateItem(Long userId, Long itemId, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id: " + itemId));
        if (req.getQuantity() != null && req.getQuantity() > 0) {
            if (!inventoryPort.isAvailable(item.getVariant().getId(), req.getQuantity())) {
                throw new BadRequestException("Requested quantity not available");
            }
            item.setQuantity(req.getQuantity());
        }
        if (req.getIsSelected() != null) {
            item.setIsSelected(req.getIsSelected());
        }
        return toDto(cartItemRepository.save(item));
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id: " + itemId));
        cartItemRepository.delete(item);
    }

    /**
     * Returns the selected items for checkout and removes them from the cart.
     */
    @Transactional
    public List<CartItem> consumeSelectedForOrder(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartIdAndIsSelectedTrue(cart.getId());
        if (items.isEmpty()) {
            throw new BadRequestException("No cart items selected for checkout");
        }
        cartItemRepository.deleteAll(items);
        return items;
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id: " + userId));
            return cartRepository.save(Cart.builder().user(user).build());
        });
    }

    private static BigDecimal snapshotPrice(ProductVariant v) {
        return v.getSalePrice() != null ? v.getSalePrice() : v.getPrice();
    }

    private static CartItemDto toDto(CartItem item) {
        BigDecimal unit = item.getPriceSnapshot();
        BigDecimal line = unit == null ? BigDecimal.ZERO
                : unit.multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemDto.builder()
                .id(item.getId())
                .variantId(item.getVariant().getId())
                .variantName(item.getVariant().getVariantName())
                .productName(item.getVariant().getProduct() == null
                        ? null : item.getVariant().getProduct().getName())
                .thumbnailUrl(item.getVariant().getImageUrl())
                .quantity(item.getQuantity())
                .priceSnapshot(unit)
                .lineTotal(line)
                .isSelected(item.getIsSelected())
                .addedAt(item.getAddedAt())
                .build();
    }
}
