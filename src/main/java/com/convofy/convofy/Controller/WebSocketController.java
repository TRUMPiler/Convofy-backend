package com.convofy.convofy.Controller;


import com.convofy.convofy.dto.UserBasicInfoDTO;
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
     public void notifyUser(String userId, UserBasicInfoDTO partnerInfo, String meetingId,String sessionid) {
         Map<String, String> payload = new HashMap<>();
         payload.put("meetId", meetingId);
         payload.put("message", "You have been matched!");
         payload.put("userId", userId); // The recipient user's ID
         payload.put("partnerId", partnerInfo.getUserId().toString()); // Partner's ID
         payload.put("partnerName", partnerInfo.getName()); // Partner's name
         payload.put("partnerAvatar", partnerInfo.getAvatar()); // Partner's avatar
         payload.put("sessionId", sessionid);
         System.out.println("Sending match notification to user: " + userId + " for meetId: " + meetingId + " with partner: " + partnerInfo.getName());
         messagingTemplate.convertAndSend("/queue/matches", payload);
     }
}
