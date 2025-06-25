package com.convofy.convofy.config;

import com.convofy.convofy.security.UserPrincipal;
import com.convofy.convofy.Service.ChatRoomPresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @Autowired
    private ChatRoomPresenceService chatRoomPresenceService;

    /**
     * Logs when a new WebSocket session connects.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Authentication authentication = (Authentication) headerAccessor.getUser();
        UserPrincipal userPrincipal = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            userPrincipal = (UserPrincipal) authentication.getPrincipal();
        }

        if (userPrincipal != null) {
            System.out.println("WebSocket connected: Session ID - " + sessionId + ", User - " + userPrincipal.getName() + " (" + userPrincipal.getUserId() + ")");
        } else {
            System.out.println("WebSocket connected: Session ID - " + sessionId + " (Unauthenticated)");
        }
    }

    /**
     * Cleans up user presence data when a WebSocket session disconnects.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        UserPrincipal userPrincipal = (UserPrincipal) headerAccessor.getUser();

        if (userPrincipal != null) {
            System.out.println("WebSocket disconnected: Session ID - " + sessionId + ", User - " + userPrincipal.getName() + " (" + userPrincipal.getUserId() + ")");
            chatRoomPresenceService.handleSessionDisconnect(sessionId);
        } else {
            System.out.println("WebSocket disconnected: Session ID - " + sessionId + " (Unauthenticated or Principal lost)");
        }
    }

    /**
     * Logs broker availability changes.
     */
    @EventListener
    public void handleBrokerAvailabilityEvent(BrokerAvailabilityEvent event) {
        System.out.println("STOMP Broker Availability: " + event.isBrokerAvailable());
    }
}
