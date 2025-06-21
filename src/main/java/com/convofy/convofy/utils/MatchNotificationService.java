package com.convofy.convofy.utils;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MatchNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public MatchNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyUser(String userId, String meetingId, String matchedUserId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("meetId", meetingId);
        payload.put("matchedUserId", matchedUserId);
        payload.put("message", "You have been matched!");
        System.out.println("Sending message to user: " + userId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/matches", payload);
    }

}
