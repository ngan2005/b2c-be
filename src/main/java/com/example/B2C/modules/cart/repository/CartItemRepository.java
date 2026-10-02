package com.example.B2C.modules.cart.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.cart.entity.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends BaseRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    List<CartItem> findByCartIdAndIsSelectedTrue(Long cartId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);
}
