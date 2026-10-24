package com.example.B2C.modules.wishlist.repository;

import com.example.B2C.modules.wishlist.entity.Wishlist;
import com.example.B2C.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @EntityGraph(attributePaths = {"product", "product.seller"})
    Page<Wishlist> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Wishlist> findByUserAndProduct_Id(User user, Long productId);

    boolean existsByUserAndProduct_Id(User user, Long productId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user = :user AND w.product.id = :productId")
    int deleteByUserAndProductId(@Param("user") User user, @Param("productId") Long productId);
}
