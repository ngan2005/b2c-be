package com.example.B2C.modules.chat.service;

import com.example.B2C.common.exception.BadRequestException;
import com.example.B2C.common.exception.ForbiddenException;
import com.example.B2C.common.exception.ResourceNotFoundException;
import com.example.B2C.common.response.PageResponse;
import com.example.B2C.modules.catalog.entity.Product;
import com.example.B2C.modules.catalog.repository.ProductRepository;
import com.example.B2C.modules.chat.dto.ConversationDto;
import com.example.B2C.modules.chat.dto.MessageDto;
import com.example.B2C.modules.chat.dto.SendMessageRequest;
import com.example.B2C.modules.chat.entity.Conversation;
import com.example.B2C.modules.chat.entity.Message;
import com.example.B2C.modules.chat.entity.MessageType;
import com.example.B2C.modules.chat.entity.SenderType;
import com.example.B2C.modules.chat.repository.ConversationRepository;
import com.example.B2C.modules.chat.repository.MessageRepository;
import com.example.B2C.modules.order.entity.Order;
import com.example.B2C.modules.order.repository.OrderRepository;
import com.example.B2C.modules.seller.entity.Seller;
import com.example.B2C.modules.seller.entity.SellerStatus;
import com.example.B2C.modules.seller.repository.SellerRepository;
import com.example.B2C.modules.user.entity.User;
import com.example.B2C.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Self-reference (via Spring proxy) so calls into
     * {@link #markConversationAsRead} start a fresh write transaction
     * instead of inheriting the outer readOnly context. Direct
     * self-invocation would bypass the proxy and silently keep
     * the readOnly flag.
     */
    @Autowired
    @Lazy
    private ChatService self;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Get or create a conversation between a buyer and a seller.
     */
    @Transactional
    public ConversationDto getOrCreateConversation(Long buyerId, Long sellerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id: " + buyerId));

        Conversation conv = conversationRepository.findByBuyerIdAndSellerId(buyerId, sellerId)
                .orElseGet(() -> {
                    Seller seller = sellerRepository.findById(sellerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Seller", "id: " + sellerId));
                    return conversationRepository.save(Conversation.builder()
                            .buyer(buyer)
                            .seller(seller)
                            .buyerUnreadCount(0)
                            .sellerUnreadCount(0)
                            .build());
                });

        return ConversationDto.from(conv, buyerId);
    }

    /**
     * List conversations for the current user (buyer or seller).
     * If a buyer has no conversations yet, automatically seeds welcome conversations
     * with active sellers to provide an immediate active chat experience.
     */
    @Transactional
    public PageResponse<ConversationDto> listConversations(Long userId, int page, int size, String role) {
        Page<Conversation> pageResult;
        if ("SELLER".equalsIgnoreCase(role)) {
            Seller seller = sellerRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
            pageResult = conversationRepository.findBySellerIdOrderByLastMessageAtDesc(
                    seller.getId(), PageRequest.of(page, Math.min(size, 50)));
        } else {
            if (page == 0 && conversationRepository.countByBuyerId(userId) == 0) {
                seedWelcomeConversations(userId);
            }
            pageResult = conversationRepository.findByBuyerIdOrderByLastMessageAtDesc(
                    userId, PageRequest.of(page, Math.min(size, 50)));
        }
        return PageResponse.of(pageResult.map(c -> ConversationDto.from(c, userId)));
    }

    /**
     * Seed initial welcome conversations for new buyers with top active sellers.
     */
    @Transactional
    public void seedWelcomeConversations(Long buyerId) {
        User buyer = userRepository.findById(buyerId).orElse(null);
        if (buyer == null) return;

        var activeSellers = sellerRepository.findAll().stream()
                .filter(s -> s.getStatus() == SellerStatus.ACTIVE)
                .limit(5)
                .toList();

        for (Seller seller : activeSellers) {
            String welcomeText = "Chào bạn! Cảm ơn bạn đã ghé thăm " + seller.getShopName() + ". Shop có thể hỗ trợ gì cho bạn hôm nay ạ?";
            Conversation conv = Conversation.builder()
                    .buyer(buyer)
                    .seller(seller)
                    .lastMessage(welcomeText)
                    .lastMessageAt(LocalDateTime.now())
                    .buyerUnreadCount(1)
                    .sellerUnreadCount(0)
                    .build();
            Conversation savedConv = conversationRepository.save(conv);

            Message welcomeMsg = Message.builder()
                    .conversation(savedConv)
                    .sender(seller.getUser() != null ? seller.getUser() : buyer)
                    .senderType(SenderType.SELLER)
                    .content(welcomeText)
                    .messageType(MessageType.TEXT)
                    .isRead(false)
                    .build();
            messageRepository.save(welcomeMsg);
        }
    }

    /**
     * Get a single conversation by ID, verifying the current user is a participant.
     */
    public ConversationDto getConversation(Long userId, Long conversationId, String role) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id: " + conversationId));

        verifyParticipant(conv, userId, role);
        return ConversationDto.from(conv, userId);
    }

    /**
     * Send a message — creates the conversation if needed.
     */
    @Transactional
    public MessageDto sendMessage(Long userId, String role, SendMessageRequest req) {
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id: " + userId));

        Conversation conv;

        if (req.getConversationId() != null && req.getConversationId() > 0) {
            conv = conversationRepository.findById(req.getConversationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id: " + req.getConversationId()));
            verifyParticipant(conv, userId, role);
        } else if (req.getSellerId() != null && req.getSellerId() > 0) {
            conv = getOrCreateConversationInternal(sender.getId(), req.getSellerId());
        } else {
            throw new BadRequestException("Either conversationId or sellerId must be provided");
        }

        SenderType senderType = "SELLER".equalsIgnoreCase(role) ? SenderType.SELLER : SenderType.BUYER;

        Message message = Message.builder()
                .conversation(conv)
                .sender(sender)
                .senderType(senderType)
                .content(req.getContent())
                .messageType(req.getMessageType() != null ? req.getMessageType() : MessageType.TEXT)
                .attachmentUrl(req.getAttachmentUrl())
                .isRead(false)
                .build();

        // Resolve product reference if provided
        if (req.getRefProductId() != null) {
            Product product = productRepository.findByIdAndDeletedAtIsNull(req.getRefProductId()).orElse(null);
            message.setRefProduct(product);
        }

        // Resolve order reference if provided
        if (req.getRefOrderId() != null) {
            Order order = orderRepository.findById(req.getRefOrderId()).orElse(null);
            message.setRefOrder(order);
        }

        Message saved = messageRepository.save(message);

        // Update conversation metadata
        conv.setLastMessage(truncate(req.getContent(), 100));
        conv.setLastMessageAt(LocalDateTime.now());

        // Increment unread count for the recipient
        if (senderType == SenderType.BUYER) {
            conv.setSellerUnreadCount(conv.getSellerUnreadCount() + 1);
        } else {
            conv.setBuyerUnreadCount(conv.getBuyerUnreadCount() + 1);
        }

        conversationRepository.save(conv);

        MessageDto dto = MessageDto.from(saved);
        broadcastNewMessage(conv, dto);

        log.info("Message {} sent by user {} in conversation {}", saved.getId(), userId, conv.getId());
        return dto;
    }

    /**
     * List messages in a conversation (newest last for display).
     * Automatically marks messages as read for the current participant.
     */
    @Transactional
    public PageResponse<MessageDto> listMessages(Long userId, String role, Long conversationId, int page, int size) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id: " + conversationId));
        verifyParticipant(conv, userId, role);

        boolean isBuyer = !"SELLER".equalsIgnoreCase(role);
        messageRepository.markAsRead(conversationId, userId, isBuyer, LocalDateTime.now());

        if (isBuyer) {
            conv.setBuyerUnreadCount(0);
        } else {
            conv.setSellerUnreadCount(0);
        }
        conversationRepository.saveAndFlush(conv);
        broadcastConversationRead(conv, isBuyer ? "BUYER" : "SELLER");

        // Fetch in ascending order for display
        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(
                        conversationId, PageRequest.of(page, Math.min(size, 50)));
        return PageResponse.of(messages.map(MessageDto::from));
    }

    private Conversation getOrCreateConversationInternal(Long buyerId, Long sellerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id: " + buyerId));
        return conversationRepository.findByBuyerIdAndSellerId(buyerId, sellerId)
                .orElseGet(() -> {
                    Seller seller = sellerRepository.findById(sellerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Seller", "id: " + sellerId));
                    return conversationRepository.save(Conversation.builder()
                            .buyer(buyer)
                            .seller(seller)
                            .buyerUnreadCount(0)
                            .sellerUnreadCount(0)
                            .build());
                });
    }

    /**
     * Mark conversation messages as read.
     */
    @Transactional
    public int markConversationAsRead(Long conversationId, Long userId, LocalDateTime now) {
        return messageRepository.markAsRead(conversationId, userId, now);
    }

    /**
     * Mark an entire conversation as read for the current participant and reset unread count.
     */
    @Transactional
    public void markConversationAsRead(Long userId, String role, Long conversationId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id: " + conversationId));
        verifyParticipant(conv, userId, role);

        boolean isBuyer = !"SELLER".equalsIgnoreCase(role);
        messageRepository.markAsRead(conversationId, userId, isBuyer, LocalDateTime.now());

        if (isBuyer) {
            conv.setBuyerUnreadCount(0);
        } else {
            conv.setSellerUnreadCount(0);
        }
        conversationRepository.saveAndFlush(conv);
        broadcastConversationRead(conv, isBuyer ? "BUYER" : "SELLER");
    }

    private void broadcastNewMessage(Conversation conv, MessageDto dto) {
        if (messagingTemplate == null || conv == null) return;
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("type", "NEW_MESSAGE");
            envelope.put("conversationId", conv.getId());
            envelope.put("sellerId", conv.getSeller() != null ? conv.getSeller().getId() : null);
            envelope.put("shopName", conv.getSeller() != null ? conv.getSeller().getShopName() : null);
            envelope.put("shopAvatar", conv.getSeller() != null ? conv.getSeller().getLogoUrl() : null);
            envelope.put("buyerUnreadCount", conv.getBuyerUnreadCount());
            envelope.put("sellerUnreadCount", conv.getSellerUnreadCount());
            envelope.put("message", dto);

            Long buyerUserId = conv.getBuyer() != null ? conv.getBuyer().getId() : null;
            Long sellerUserId = (conv.getSeller() != null && conv.getSeller().getUser() != null)
                    ? conv.getSeller().getUser().getId()
                    : null;

            if (buyerUserId != null) {
                messagingTemplate.convertAndSend("/topic/chat/user/" + buyerUserId, (Object) envelope);
            }
            if (sellerUserId != null && !sellerUserId.equals(buyerUserId)) {
                messagingTemplate.convertAndSend("/topic/chat/user/" + sellerUserId, (Object) envelope);
            }
            messagingTemplate.convertAndSend("/topic/chat/conversation/" + conv.getId(), (Object) envelope);
        } catch (Exception ex) {
            log.warn("Failed to broadcast chat message for conversation {}: {}", conv.getId(), ex.getMessage());
        }
    }

    private void broadcastConversationRead(Conversation conv, String readerRole) {
        if (messagingTemplate == null || conv == null) return;
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("type", "CONVERSATION_READ");
            envelope.put("conversationId", conv.getId());
            envelope.put("readerRole", readerRole);
            envelope.put("buyerUnreadCount", conv.getBuyerUnreadCount());
            envelope.put("sellerUnreadCount", conv.getSellerUnreadCount());

            Long buyerUserId = conv.getBuyer() != null ? conv.getBuyer().getId() : null;
            Long sellerUserId = (conv.getSeller() != null && conv.getSeller().getUser() != null)
                    ? conv.getSeller().getUser().getId()
                    : null;

            if (buyerUserId != null) {
                messagingTemplate.convertAndSend("/topic/chat/user/" + buyerUserId, (Object) envelope);
            }
            if (sellerUserId != null && !sellerUserId.equals(buyerUserId)) {
                messagingTemplate.convertAndSend("/topic/chat/user/" + sellerUserId, (Object) envelope);
            }
            messagingTemplate.convertAndSend("/topic/chat/conversation/" + conv.getId(), (Object) envelope);
        } catch (Exception ex) {
            log.warn("Failed to broadcast conversation read for {}: {}", conv.getId(), ex.getMessage());
        }
    }

    private void verifyParticipant(Conversation conv, Long userId, String role) {
        if ("SELLER".equalsIgnoreCase(role)) {
            Seller seller = sellerRepository.findByUserId(userId).orElse(null);
            if (seller == null || !seller.getId().equals(conv.getSeller().getId())) {
                throw new ForbiddenException("You are not a participant in this conversation");
            }
        } else {
            if (!userId.equals(conv.getBuyer().getId())) {
                throw new ForbiddenException("You are not a participant in this conversation");
            }
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
