package com.example.B2C.modules.chat.dto;

import com.example.B2C.modules.chat.entity.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send a message in a conversation")
public class SendMessageRequest {

    @Schema(description = "Conversation ID (required to continue an existing conversation; null to start a new one with the target seller)", example = "42")
    private Long conversationId;

    @Schema(description = "Seller ID to start a new conversation with (required if conversationId is null)", example = "5")
    private Long sellerId;

    @NotBlank(message = "Message content is required")
    @Schema(description = "Text content of the message", example = "Hi, is this product still available?")
    private String content;

    @Builder.Default
    @Schema(description = "Message type: TEXT, IMAGE, PRODUCT, ORDER", example = "TEXT")
    private MessageType messageType = MessageType.TEXT;

    @Schema(description = "URL of an attached image (required if messageType=IMAGE)", example = "https://example.com/img.jpg")
    private String attachmentUrl;

    @Schema(description = "Product ID to reference (used when messageType=PRODUCT)", example = "10")
    private Long refProductId;

    @Schema(description = "Order ID to reference (used when messageType=ORDER)", example = "7")
    private Long refOrderId;
}
