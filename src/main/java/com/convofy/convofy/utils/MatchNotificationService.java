package com.convofy.convofy.utils;

import com.datastax.oss.driver.internal.core.type.codec.SmallIntCodec;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public MatchNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyUser(String userId, String message) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/matches", message);
    }
}
