package com.example.B2C.modules.seller.repository;

import com.example.B2C.common.repository.BaseRepository;
import com.example.B2C.modules.seller.entity.Seller;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends BaseRepository<Seller, Long> {

    Optional<Seller> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsBySlug(String slug);

    Optional<Seller> findBySlug(String slug);
}
