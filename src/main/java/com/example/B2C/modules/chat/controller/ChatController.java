package com.example.B2C.modules.chat.controller;

import com.example.B2C.common.response.ApiResponse;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.common.security.CurrentUserProvider;
import com.example.B2C.modules.chat.dto.ConversationDto;
import com.example.B2C.modules.chat.dto.MessageDto;
import com.example.B2C.modules.chat.dto.SendMessageRequest;
import com.example.B2C.modules.chat.service.ChatService;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.repository.SellerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Chat", description = "Buyer-Seller messaging and conversation management")
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserProvider currentUserProvider;
    private final SellerRepository sellerRepository;

    private String detectRole(Long userId) {
        return sellerRepository.findByUserId(userId).isPresent() ? "SELLER" : "BUYER";
    }

    @GetMapping("/buyer/conversations")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(
            summary = "List buyer's conversations",
            description = "Returns all conversations the authenticated buyer participates in, "
                    + "sorted by most recent message first."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversations retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not a buyer")
    })
    public ResponseEntity<ApiResponse<PageResponse<ConversationDto>>> listBuyerConversations(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)") @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                chatService.listConversations(userId, page, size, "BUYER")));
    }

    @GetMapping("/seller/conversations")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "List seller's conversations (newest first)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversations retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User is not a seller")
    })
    public ResponseEntity<ApiResponse<PageResponse<ConversationDto>>> listSellerConversations(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)") @RequestParam(defaultValue = "20") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                chatService.listConversations(userId, page, size, "SELLER")));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get a conversation by ID (buyer or seller)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a participant"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public ResponseEntity<ApiResponse<ConversationDto>> getConversation(@PathVariable Long id) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                chatService.getConversation(userId, id, detectRole(userId))));
    }

    @PostMapping("/conversations")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(
            summary = "Start a new conversation with a seller",
            description = "Creates or returns an existing buyer-seller conversation."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Conversation created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Seller not found")
    })
    public ResponseEntity<ApiResponse<ConversationDto>> startConversation(
            @RequestParam Long sellerId) {
        Long buyerId = currentUserProvider.getCurrentUserId();
        ConversationDto dto = chatService.getOrCreateConversation(buyerId, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(
            summary = "List messages in a conversation",
            description = "Returns messages sorted oldest-first (newest at the bottom of the chat). "
                    + "Automatically marks messages as read for the current user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a participant"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<MessageDto>>> listMessages(
            @Parameter(description = "Conversation ID") @PathVariable Long id,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)") @RequestParam(defaultValue = "50") int size) {
        Long userId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                chatService.listMessages(userId, detectRole(userId), id, page, size)));
    }

    @PostMapping("/conversations/{id}/messages")
    @Operation(
            summary = "Send a message in a conversation",
            description = "Sends a message as the authenticated buyer or seller. "
                    + "Creates the conversation if conversationId is not provided but sellerId is."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Message sent"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not a participant"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation or reference not found")
    })
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @Parameter(description = "Conversation ID") @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        // Ensure conversationId is set for sendMessage
        if (request.getConversationId() == null) {
            request.setConversationId(id);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                chatService.sendMessage(userId, detectRole(userId), request)));
    }
}
