package com.example.B2C.modules.chat.repository;

import com.example.B2C.modules.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByBuyerIdAndSellerId(Long buyerId, Long sellerId);

    @Query("SELECT c FROM Conversation c WHERE c.buyer.id = :buyerId ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findByBuyerIdOrderByLastMessageAtDesc(@Param("buyerId") Long buyerId, Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE c.seller.id = :sellerId ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findBySellerIdOrderByLastMessageAtDesc(@Param("sellerId") Long sellerId, Pageable pageable);
}
