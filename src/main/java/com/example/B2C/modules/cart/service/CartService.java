package com.example.B2C.modules.cart.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.port.InventoryPort;
import com.example.B2C.modules.catalog.api.CatalogFacade;
import com.example.B2C.modules.catalog.api.dto.ProductVariantView;
import com.example.B2C.modules.cart.dto.CartItemDto;
import com.example.B2C.modules.cart.dto.AddCartItemRequest;
import com.example.B2C.modules.cart.dto.UpdateCartItemRequest;
import com.example.B2C.modules.cart.entity.Cart;
import com.example.B2C.modules.cart.entity.CartItem;
import com.example.B2C.modules.cart.repository.CartItemRepository;
import com.example.B2C.modules.cart.repository.CartRepository;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import com.example.B2C.modules.catalog.entity.ProductVariant;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CatalogFacade catalogFacade;
    private final InventoryPort inventoryPort;
    private final EntityManager entityManager;

    @Transactional
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
        ProductVariantView variant = catalogFacade.getVariant(req.getVariantId());
        if (!inventoryPort.isAvailable(req.getVariantId(), req.getQuantity())) {
            throw new BadRequestException("Requested quantity not available");
        }
        Cart cart = getOrCreateCart(userId);
        ProductVariant variantProxy = entityManager.getReference(ProductVariant.class, req.getVariantId());
        CartItem item = cartItemRepository.findByCartIdAndVariantId(cart.getId(), req.getVariantId())
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .variant(variantProxy)
                        .variantId(req.getVariantId())
                        .variantName(variant.variantName())
                        .productName(variant.productName())
                        .imageUrl(variant.imageUrl())
                        .priceSnapshot(variant.unitPrice())
                        .quantity(0)
                        .isSelected(true)
                        .build());
        item.setQuantity(item.getQuantity() + req.getQuantity());
        item.setPriceSnapshot(variant.unitPrice());
        if (item.getVariant() == null) {
            item.setVariant(variantProxy);
        }
        if (item.getVariantId() == null) {
            item.setVariantId(req.getVariantId());
        }
        CartItem saved = cartItemRepository.save(item);
        return toDto(saved);
    }

    @Transactional
    public CartItemDto updateItem(Long userId, Long itemId, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id: " + itemId));
        Long varId = item.getVariantId();
        if (req.getQuantity() != null && req.getQuantity() > 0) {
            if (!inventoryPort.isAvailable(varId, req.getQuantity())) {
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id: " + userId));
            return cartRepository.save(Cart.builder().user(user).build());
        });
    }

    private static CartItemDto toDto(CartItem item) {
        BigDecimal unit = item.getPriceSnapshot();
        BigDecimal line = unit == null ? BigDecimal.ZERO
                : unit.multiply(BigDecimal.valueOf(item.getQuantity()));
        Long varId = item.getVariantId();
        return CartItemDto.builder()
                .id(item.getId())
                .variantId(varId)
                .variantName(item.getVariantName())
                .productName(item.getProductName())
                .thumbnailUrl(item.getImageUrl())
                .quantity(item.getQuantity())
                .priceSnapshot(unit)
                .lineTotal(line)
                .isSelected(item.getIsSelected())
                .addedAt(item.getAddedAt())
                .build();
    }
}
