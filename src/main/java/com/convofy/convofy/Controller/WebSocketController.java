package com.convofy.convofy.Controller;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;



     // Sends a message every 5 seconds
//    public void sendMessages() {
//        System.out.println("Sending messages");
//        String message = "Message #" + counter++;
//        messagingTemplate.convertAndSend("/topic/updates", message);
//    }
    public void notifyUser(String userId, String useremail,String meetingId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("meetId", meetingId);
        payload.put("message", "You have been matched!");
        payload.put("userId", userId);
        payload.put("useremail", useremail);
        System.out.println("Sending message to user: " + userId);
        messagingTemplate.convertAndSend("/queue/matches", payload);
    }
}
