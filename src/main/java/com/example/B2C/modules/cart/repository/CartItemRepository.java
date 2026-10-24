package com.example.B2C.modules.cart.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends BaseRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    List<CartItem> findByCartIdAndIsSelectedTrue(Long cartId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND (ci.variant.id = :variantId OR ci.variantId = :variantId)")
    Optional<CartItem> findByCartIdAndVariantId(@Param("cartId") Long cartId, @Param("variantId") Long variantId);
}

