package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.User;
import com.convofy.convofy.security.UserPrincipal;
import com.convofy.convofy.Entity.ChatMessage;
import com.convofy.convofy.Entity.Interest;
import com.convofy.convofy.Repository.ChatMessageRepository;
import com.convofy.convofy.Repository.InterestRepository;
import com.convofy.convofy.Repository.UserRepository;
import com.convofy.convofy.dto.ChatMessageResponseDTO;
import com.convofy.convofy.dto.ClientMessageDTO;
import com.convofy.convofy.dto.JoinRoomRequest;
import com.convofy.convofy.Service.ChatRoomPresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.data.domain.PageRequest; // For creating Pageable instances
import org.springframework.data.domain.Pageable;   // For Pageable interface
import org.springframework.data.domain.Sort;      // For sorting results

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
// Changed from @Controller to @RestController
import org.springframework.web.bind.annotation.RestController; // Changed from @Controller
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping; // Import RequestMapping

import java.time.Instant;

@RestController // Use @RestController for RESTful APIs
@RequestMapping("/api") // Add a class-level RequestMapping for /api base path
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatRoomPresenceService chatRoomPresenceService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestRepository interestRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ClientMessageDTO clientMessage, SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication = (Authentication) headerAccessor.getUser(); // First, cast to Authentication
        UserPrincipal userPrincipal = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            userPrincipal = (UserPrincipal) authentication.getPrincipal(); // Then, get the principal from it
        }

        if (userPrincipal == null) {
            System.err.println("Unauthenticated or invalid principal user tried to send message.");
            return;
        }

        UUID chatroomId = clientMessage.getChatroomId();
        UUID senderId = userPrincipal.getUserId();
        String content = clientMessage.getContent();

        Optional<Interest> chatroomOptional = interestRepository.findById(chatroomId);
        if (chatroomOptional.isEmpty()) {
            System.err.println("Chatroom " + chatroomId + " not found. Message not sent by " + userPrincipal.getName());
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatroomId(chatroomId); // Correct: Pass UUID directly
        chatMessage.setSenderId(senderId);     // Correct: Pass UUID directly
        chatMessage.setContent(content);
        chatMessage.setTimestamp(Instant.now()); // Correct: Pass Instant directly

        chatMessageRepository.save(chatMessage);
        System.out.println("Saved message: " + chatMessage.getMessageId() + " from " + userPrincipal.getName() + " to room " + chatroomId);

        ChatMessageResponseDTO messageResponse = new ChatMessageResponseDTO(
                chatMessage.getMessageId().toString(),
                chatMessage.getSenderId().toString(),
                userPrincipal.getName(),
                userPrincipal.getImage() != null ? userPrincipal.getImage() : "https://github.com/shadcn.png",
                chatMessage.getContent(),
                chatMessage.getTimestamp().toString()
        );

        messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/messages", messageResponse);
        System.out.println("Broadcasted message to /topic/chatroom/" + chatroomId + "/messages");
    }

    // Now this endpoint will be accessible at /api/chat/history/{chatroomId}
    @GetMapping("/chat/history/{chatroomId}")
    public ResponseEntity<com.convofy.convofy.utils.Response<List<ChatMessageResponseDTO>>> getChatHistory(
            @PathVariable UUID chatroomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        try {
            if (!interestRepository.existsById(chatroomId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new com.convofy.convofy.utils.Response<>(false, "Chatroom not found", null));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
            List<ChatMessage> chatMessages = chatMessageRepository.findByChatroomIdOrderByTimestampDesc(chatroomId, pageable);


            Set<UUID> senderIds = chatMessages.stream()
                    .map(ChatMessage::getSenderId)
                    .collect(Collectors.toSet());

            List<User> senders = userRepository.findAllById(senderIds);

            Map<UUID, User> senderMap = senders.stream()
                    .collect(Collectors.toMap(User::getUserId, user -> user));

            List<ChatMessageResponseDTO> messageDTOs = chatMessages.stream().map(msg -> {
                String senderName = "Unknown User";
                String senderAvatar = "https://github.com/shadcn.png"; // Default avatar

                User sender = senderMap.get(msg.getSenderId());
                if (sender != null) {
                    senderName = sender.getName();
                    senderAvatar = sender.getImage() != null ? sender.getImage() : "https://github.com/shadcn.png";
                }

                return new ChatMessageResponseDTO(
                        msg.getMessageId().toString(),
                        msg.getSenderId().toString(),
                        senderName,
                        senderAvatar,
                        msg.getContent(),
                        msg.getTimestamp().toString()
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok(new com.convofy.convofy.utils.Response<>(true, "Chat history retrieved successfully", messageDTOs));
        } catch (Exception e) {
            System.err.println("Error fetching chat history for room " + chatroomId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // Changed from OK to INTERNAL_SERVER_ERROR
                    .body(new com.convofy.convofy.utils.Response<>(false, "Failed to retrieve chat history: " + e.getMessage(), null));
        }
    }

    @MessageMapping("/chat.joinRoom")
    public void joinRoom(@Payload JoinRoomRequest joinRequest, SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication = (Authentication) headerAccessor.getUser(); // First, cast to Authentication
        UserPrincipal userPrincipal = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            userPrincipal = (UserPrincipal) authentication.getPrincipal(); // Then, get the principal from it
        }

        String sessionId = headerAccessor.getSessionId();

        if (userPrincipal == null) {
            System.err.println("Unauthenticated or invalid principal user tried to join room.");
            return;
        }

        UUID chatroomId = joinRequest.getChatroomId();

        Optional<Interest> chatroomOptional = interestRepository.findById(chatroomId);
        if (chatroomOptional.isEmpty()) {
            System.err.println("Chatroom " + chatroomId + " not found for join request by " + userPrincipal.getName());
            return;
        }

        chatRoomPresenceService.userJoinRoom(chatroomId, userPrincipal, sessionId);
        System.out.println("STOMP: User " + userPrincipal.getName() + " requested to join room " + chatroomId);
    }

    @MessageMapping("/chat.leaveRoom")
    public void leaveRoom(@Payload JoinRoomRequest leaveRequest, SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication = (Authentication) headerAccessor.getUser(); // First, cast to Authentication
        UserPrincipal userPrincipal = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            userPrincipal = (UserPrincipal) authentication.getPrincipal(); // Then, get the principal from it
        }

        String sessionId = headerAccessor.getSessionId();

        if (userPrincipal == null) {
            System.err.println("Unauthenticated or invalid principal user tried to leave room.");
            return;
        }

        UUID chatroomId = leaveRequest.getChatroomId();

        if (!interestRepository.existsById(chatroomId)) {
            System.err.println("Chatroom " + chatroomId + " not found for leave request by " + userPrincipal.getName());
            return;
        }

        chatRoomPresenceService.userLeaveRoom(chatroomId, userPrincipal.getUserId(), sessionId);
        System.out.println("STOMP: User " + userPrincipal.getName() + " requested to leave room " + chatroomId);
    }
}
