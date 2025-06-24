package com.convofy.convofy.security.websocket;

import com.convofy.convofy.Repository.UserRepository;
import com.convofy.convofy.Entity.User;
import com.convofy.convofy.security.UserPrincipal; // This is the UserPrincipal class we'll need to define
import com.convofy.convofy.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository; // To fetch user details from DB for Principal

    // This method is called before a message is sent to the channel
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Only process CONNECT messages (WebSocket handshake)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String jwt = authorizationHeader.substring(7);
                try {
                    String userEmail = jwtUtil.extractUsername(jwt);

                    // Validate JWT and load user from DB
                    Optional<User> userOptional = userRepository.findByEmail(userEmail);

                    if (userOptional.isPresent() && jwtUtil.validateToken(jwt, new UserPrincipal(userOptional.get()))) {
                        // Create an Authentication object for the WebSocket session
                        User user = userOptional.get();
                        UserPrincipal userPrincipal = new UserPrincipal(user); // Use UserPrincipal for detailed user info
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userPrincipal, null, userPrincipal.getAuthorities());

                        // Set the authenticated principal on the WebSocket session
                        accessor.setUser(authentication);
                    } else {
                        // JWT is invalid or user not found, reject connection or proceed unauthenticated
                        System.out.println("Invalid JWT or user not found for WebSocket: " + userEmail);
                        // accessor.setLeaveMutable(true); // Can uncomment to reject directly if needed
                        // accessor.setException(new Exception("Unauthorized"));
                    }
                } catch (Exception e) {
                    System.out.println("WebSocket JWT validation error: " + e.getMessage());
                    // accessor.setLeaveMutable(true); // Can uncomment to reject directly if needed
                    // accessor.setException(new Exception("JWT Error: " + e.getMessage()));
                }
            } else {
                System.out.println("No JWT found in WebSocket CONNECT header.");
                // Allow unauthenticated connection for now, but features will be restricted.
            }
        }
        return message;
    }
}
