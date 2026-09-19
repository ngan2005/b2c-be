package com.example.B2C.modules.chat.entity;

import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation", uniqueConstraints = {
    @UniqueConstraint(name = "uq_buyer_seller", columnNames = {"buyer_id", "seller_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "buyer_unread_count", nullable = false)
    @Builder.Default
    private Integer buyerUnreadCount = 0;

    @Column(name = "seller_unread_count", nullable = false)
    @Builder.Default
    private Integer sellerUnreadCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
