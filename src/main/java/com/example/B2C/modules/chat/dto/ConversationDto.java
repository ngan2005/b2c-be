package com.example.B2C.modules.chat.dto;

import com.example.B2C.modules.chat.entity.Conversation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Conversation summary for inbox listings")
public class ConversationDto {

    @Schema(description = "Conversation ID", example = "42")
    private Long id;

    @Schema(description = "The other party's ID (seller for buyers, buyer for sellers)", example = "5")
    private Long otherPartyId;

    @Schema(description = "Other party's display name (shop name for sellers, full name for buyers)",
            example = "Tech Gadgets Shop")
    private String otherPartyName;

    @Schema(description = "Other party's avatar URL", example = "https://example.com/shop-logo.jpg")
    private String otherPartyAvatar;

    @Schema(description = "Last message preview (truncated to 100 chars)", example = "Hi, the product is still available...")
    private String lastMessage;

    @Schema(description = "When the last message was sent", example = "2024-01-10T10:30:00")
    private LocalDateTime lastMessageAt;

    @Schema(description = "Unread message count for the current user", example = "3")
    private Integer unreadCount;

    @Schema(description = "ISO-8601 creation timestamp", example = "2024-01-09T08:00:00")
    private LocalDateTime createdAt;

    public static ConversationDto from(Conversation c, Long currentUserId) {
        boolean isBuyer = c.getBuyer() != null && c.getBuyer().getId().equals(currentUserId);
        Long otherId = isBuyer
                ? (c.getSeller() != null ? c.getSeller().getId() : null)
                : (c.getBuyer() != null ? c.getBuyer().getId() : null);
        String otherName = isBuyer
                ? (c.getSeller() != null ? c.getSeller().getShopName() : null)
                : (c.getBuyer() != null ? c.getBuyer().getFullName() : null);
        String otherAvatar = isBuyer
                ? (c.getSeller() != null ? c.getSeller().getLogoUrl() : null)
                : (c.getBuyer() != null ? c.getBuyer().getAvatarUrl() : null);
        Integer unread = isBuyer ? c.getBuyerUnreadCount() : c.getSellerUnreadCount();

        return ConversationDto.builder()
                .id(c.getId())
                .otherPartyId(otherId)
                .otherPartyName(otherName)
                .otherPartyAvatar(otherAvatar)
                .lastMessage(c.getLastMessage())
                .lastMessageAt(c.getLastMessageAt())
                .unreadCount(unread)
                .createdAt(c.getCreatedAt())
                .build();
    }
}
