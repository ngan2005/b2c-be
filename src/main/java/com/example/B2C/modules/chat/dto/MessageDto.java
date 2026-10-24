package com.example.B2C.modules.chat.dto;

import com.example.B2C.modules.chat.entity.Conversation;
import com.example.B2C.modules.chat.entity.MessageType;
import com.example.B2C.modules.chat.entity.SenderType;
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
@Schema(description = "Message response payload")
public class MessageDto {

    @Schema(description = "Message ID", example = "1")
    private Long id;

    @Schema(description = "Conversation ID", example = "42")
    private Long conversationId;

    @Schema(description = "ID of the sender", example = "3")
    private Long senderId;

    @Schema(description = "SENDER_TYPE: BUYER or SELLER", example = "BUYER")
    private SenderType senderType;

    @Schema(description = "Sender's display name", example = "Nguyen Van A")
    private String senderName;

    @Schema(description = "Message content", example = "Hi, is this product still available?")
    private String content;

    @Schema(description = "MESSAGE_TYPE enum: TEXT, IMAGE, PRODUCT, ORDER", example = "TEXT")
    private MessageType messageType;

    @Schema(description = "URL of an attached file (image, etc.)", example = "https://example.com/img.jpg")
    private String attachmentUrl;

    @Schema(description = "Referenced product ID (for PRODUCT type)", example = "10")
    private Long refProductId;

    @Schema(description = "Referenced order ID (for ORDER type)", example = "7")
    private Long refOrderId;

    @Schema(description = "Whether the message has been read", example = "false")
    private Boolean isRead;

    @Schema(description = "When the message was read", example = "2024-01-10T11:00:00")
    private LocalDateTime readAt;

    @Schema(description = "ISO-8601 creation timestamp", example = "2024-01-10T10:30:00")
    private LocalDateTime createdAt;

    public static MessageDto from(com.example.B2C.modules.chat.entity.Message msg) {
        return MessageDto.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation() != null ? msg.getConversation().getId() : null)
                .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
                .senderType(msg.getSenderType())
                .senderName(msg.getSender() != null ? msg.getSender().getFullName() : null)
                .content(msg.getContent())
                .messageType(msg.getMessageType())
                .attachmentUrl(msg.getAttachmentUrl())
                .refProductId(msg.getRefProduct() != null ? msg.getRefProduct().getId() : null)
                .refOrderId(msg.getRefOrder() != null ? msg.getRefOrder().getId() : null)
                .isRead(msg.getIsRead())
                .readAt(msg.getReadAt())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
