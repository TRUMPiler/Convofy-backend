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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.time.Instant;

@RestController
@RequestMapping("/api")
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
        Authentication authentication = (Authentication) headerAccessor.getUser();
        UserPrincipal userPrincipal = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            userPrincipal = (UserPrincipal) authentication.getPrincipal();
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
        chatMessage.setChatroomId(chatroomId);
        chatMessage.setSenderId(senderId);
        chatMessage.setContent(content);
        chatMessage.setTimestamp(Instant.now());

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
                String senderAvatar = "https://github.com/shadcn.png";

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.convofy.convofy.utils.Response<>(false, "Failed to retrieve chat history: " + e.getMessage(), null));
        }
    }
    @MessageMapping("/chat.editMessage")
    public void editMessage(@RequestBody ClientMessageDTO clientMessageDTO,SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication =(Authentication) headerAccessor.getUser();
        UserPrincipal user=null;
        if(authentication!=null&&authentication.getPrincipal() instanceof UserPrincipal){
            user=(UserPrincipal)authentication.getPrincipal();

        }
        if(user==null)
        {
            System.err.println("Unauthenticated or invalid principal user tried to delete message.");
            return;
        }
        UUID messageId=clientMessageDTO.getMessageId();
        UUID chatroomId = clientMessageDTO.getChatroomId();
        UUID editorId = user.getUserId();
        String newContent = clientMessageDTO.getContent();
        if (messageId == null || newContent == null || newContent.trim().isEmpty()) {
            System.err.println("Invalid messageId or empty content for edit by " + user.getName());
            return;
        }
        Optional<ChatMessage> existingMessageOptional = chatMessageRepository.findById(messageId);
        if (existingMessageOptional.isEmpty()) {
            System.err.println("Message with id " + messageId + " not found. Message not sent by " + user.getName());
        }
        ChatMessage existingMessage = existingMessageOptional.get();
        existingMessage.setContent(newContent+" (1)");
        chatMessageRepository.save(existingMessage);
        System.out.println("Edited message: " + messageId + " by " + user.getName() + " in room " + chatroomId);
        ChatMessageResponseDTO messageResponse = new ChatMessageResponseDTO(
                existingMessage.getMessageId().toString(),
                existingMessage.getSenderId().toString(),
                user.getName(),
                user.getImage() != null ? user.getImage() : "https://github.com/shadcn.png",
                existingMessage.getContent(),
                existingMessage.getTimestamp().toString()
        );
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/messages", messageResponse);
        System.out.println("Broadcasted edited message to /topic/chatroom/" + chatroomId + "/messages");
    }


    @MessageMapping("/chat.deleteMessage")
    public void deleteMessage(@RequestBody ClientMessageDTO clientMessageDTO,SimpMessageHeaderAccessor headerAccessor) {
        Authentication authentication = (Authentication) headerAccessor.getUser();
        UserPrincipal userPrincipal = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            userPrincipal = (UserPrincipal) authentication.getPrincipal();
        }

        if (userPrincipal == null) {
            System.err.println("Unauthenticated or invalid principal user tried to delete message.");
            return;
        }
        UUID messageId=clientMessageDTO.getMessageId();
        UUID chatroomId = clientMessageDTO.getChatroomId();
        UUID deleterId = userPrincipal.getUserId();
        if (messageId == null) {
            System.err.println("Invalid messageId for delete by " + userPrincipal.getName());
            return;
        }

        Optional<ChatMessage> existingMessageOptional = chatMessageRepository.findById(messageId);

        if (existingMessageOptional.isEmpty()) {
            System.err.println("Message " + messageId + " not found for delete by " + userPrincipal.getName());
            return;
        }
        ChatMessage existingMessage = existingMessageOptional.get();
        if (!existingMessage.getSenderId().equals(deleterId)) {
            System.err.println("User " + userPrincipal.getName() + " tried to delete message " + messageId + " but is not the sender.");
            return;
        }

        existingMessage.setContent("[Message deleted]");
        chatMessageRepository.save(existingMessage);
        System.out.println("Deleted message: " + messageId + " by " + userPrincipal.getName() + " from room " + chatroomId);
        ChatMessageResponseDTO deletionNotification = new ChatMessageResponseDTO(
                messageId.toString(),
                deleterId.toString(),
                userPrincipal.getName(),
                userPrincipal.getImage() != null ? userPrincipal.getImage() : "https://github.com/shadcn.png",
                "[Message Deleted]", // Indicate deletion
                Instant.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/messages", deletionNotification);
        System.out.println("Broadcasted edited message to /topic/chatroom/" + chatroomId + "/messages");
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
