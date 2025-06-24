package com.convofy.convofy.Controller;

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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication; // Import Authentication

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Controller
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
