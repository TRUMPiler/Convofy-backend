package com.convofy.convofy.Controller;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

@Controller
public class MeetingChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public MeetingChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public static class IncomingChatMessage {
        private String content;
        private String senderId;
        private String senderName;
        private String senderAvatar;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public String getSenderAvatar() { return senderAvatar; }
        public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    }

    public static class OutgoingChatMessage {
        private String id;
        private String senderId;
        private String senderName;
        private String senderAvatar;
        private String content;
        private String timestamp;

        public OutgoingChatMessage(String id, String senderId, String senderName, String senderAvatar, String content, String timestamp) {
            this.id = id;
            this.senderId = senderId;
            this.senderName = senderName;
            this.senderAvatar = senderAvatar;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public String getSenderId() { return senderId; }
        public String getSenderName() { return senderName; }
        public String getSenderAvatar() { return senderAvatar; }
        public String getContent() { return content; }
        public String getTimestamp() { return timestamp; }
    }

    @MessageMapping("/meeting.sendMessage/{meetingId}")
    public void sendMeetingMessage(@DestinationVariable String meetingId, @Payload IncomingChatMessage message) {
        System.out.println("Received temporary chat message for meeting " + meetingId + " from " + message.getSenderName() + ": " + message.getContent());

        OutgoingChatMessage outgoingMessage = new OutgoingChatMessage(
                UUID.randomUUID().toString(),
                message.getSenderId(),
                message.getSenderName(),
                message.getSenderAvatar(),
                message.getContent(),
                Instant.now().toString()
        );

        messagingTemplate.convertAndSend("/topic/meeting-chat/" + meetingId, outgoingMessage);
    }
}
