package com.convofy.convofy.Controller;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/notify")
    @SendTo("/queue/matches")
    public String notifyUser(String message) {
        return "Match Found! " + message;
    }
}
