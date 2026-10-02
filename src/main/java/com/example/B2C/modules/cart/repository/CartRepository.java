package com.example.B2C.modules.cart.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.cart.entity.Cart;

import java.util.Optional;

public interface CartRepository extends BaseRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);
}
