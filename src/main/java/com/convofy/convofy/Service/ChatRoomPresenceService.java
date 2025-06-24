package com.convofy.convofy.Service;

import com.convofy.convofy.security.UserPrincipal;
import com.convofy.convofy.dto.OnlineUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatRoomPresenceService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Helps us send messages to WebSocket topics

    // Keeps track of who's online in which chatroom.
    // Key: chatroomId, Value: A list of users (OnlineUserDTO) currently in that room.
    private final Map<UUID, Set<OnlineUserDTO>> onlineUsersInRooms = new ConcurrentHashMap<>();

    // Maps each WebSocket session to the chatrooms that session is currently active in.
    // Key: sessionId, Value: The IDs of chatrooms where this session is present.
    private final Map<String, Set<UUID>> sessionRoomSubscriptions = new ConcurrentHashMap<>();

    // Stores the authenticated user's details for each active WebSocket session.
    // Key: sessionId, Value: The UserPrincipal for that session.
    private final Map<String, UserPrincipal> activeSessionPrincipals = new ConcurrentHashMap<>();


    /**
     * Registers a user as online in a specific chatroom.
     * This also broadcasts the updated list of online users for that room.
     *
     * @param chatroomId The unique ID of the chatroom.
     * @param userPrincipal The details of the authenticated user.
     * @param sessionId The current WebSocket session ID.
     */
    public void userJoinRoom(UUID chatroomId, UserPrincipal userPrincipal, String sessionId) {
        // Remember which user is tied to this session, useful for disconnects
        activeSessionPrincipals.put(sessionId, userPrincipal);

        OnlineUserDTO onlineUser = new OnlineUserDTO(
                userPrincipal.getUserId(),
                userPrincipal.getName(),
                userPrincipal.getEmail(),
                userPrincipal.getImage()
        );

        // Add the user to the chatroom's online list. If the room isn't tracked yet, start tracking it.
        onlineUsersInRooms.computeIfAbsent(chatroomId, k -> ConcurrentHashMap.newKeySet()).add(onlineUser);

        // Keep a record of which rooms this specific session is active in
        sessionRoomSubscriptions.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(chatroomId);

        // Let everyone in the room know who's online now
        broadcastOnlineUsers(chatroomId);
        System.out.println("User " + userPrincipal.getName() + " joined room " + chatroomId + ". Online users: " + getOnlineUsersInRoom(chatroomId).size());
    }

    /**
     * Removes a user from a specific chatroom's online list.
     * This broadcasts the updated list of online users for that room.
     *
     * @param chatroomId The unique ID of the chatroom.
     * @param userId The ID of the user leaving.
     * @param sessionId The WebSocket session ID.
     */
    public void userLeaveRoom(UUID chatroomId, UUID userId, String sessionId) {
        Set<OnlineUserDTO> usersInRoom = onlineUsersInRooms.get(chatroomId);
        if (usersInRoom != null) {
            // Remove the user from this room's active list
            usersInRoom.removeIf(user -> user.getUserId().equals(userId));
            if (usersInRoom.isEmpty()) {
                // If the room is now empty, stop tracking it
                onlineUsersInRooms.remove(chatroomId);
            }
        }

        // Update our record of which rooms this session is in
        Set<UUID> roomsForSession = sessionRoomSubscriptions.get(sessionId);
        if (roomsForSession != null) {
            roomsForSession.remove(chatroomId);
            if (roomsForSession.isEmpty()) {
                // If this session is no longer in any rooms, remove its record
                sessionRoomSubscriptions.remove(sessionId);
            }
        }

        // Notify everyone in the room about the updated online list
        broadcastOnlineUsers(chatroomId);
        System.out.println("User " + userId + " left room " + chatroomId + ". Online users: " + getOnlineUsersInRoom(chatroomId).size());
    }

    /**
     * Cleans up when a WebSocket session fully disconnects.
     * Removes the user from all chatrooms they were in and updates those rooms' presence lists.
     *
     * @param sessionId The ID of the WebSocket session that disconnected.
     */
    public void handleSessionDisconnect(String sessionId) {
        UserPrincipal userPrincipal = activeSessionPrincipals.remove(sessionId);
        if (userPrincipal == null) {
            System.out.println("Disconnected session " + sessionId + " was not recognized or already handled.");
            return; // This session wasn't associated with a logged-in user or was already cleaned up.
        }

        UUID disconnectedUserId = userPrincipal.getUserId();
        // Find all rooms this session was subscribed to
        Set<UUID> roomsToUpdate = sessionRoomSubscriptions.remove(sessionId);

        if (roomsToUpdate != null) {
            System.out.println("User " + disconnectedUserId + " (session " + sessionId + ") disconnected. Cleaning up from rooms: " + roomsToUpdate);
            for (UUID chatroomId : roomsToUpdate) {
                Set<OnlineUserDTO> usersInRoom = onlineUsersInRooms.get(chatroomId);
                if (usersInRoom != null) {
                    // Remove the disconnected user from this room
                    usersInRoom.removeIf(user -> user.getUserId().equals(disconnectedUserId));
                    if (usersInRoom.isEmpty()) {
                        // If the room is now empty, remove it from our tracking
                        onlineUsersInRooms.remove(chatroomId);
                        System.out.println("Chatroom " + chatroomId + " is now empty.");
                    }
                }
                // Broadcast the updated online list for this room
                broadcastOnlineUsers(chatroomId);
            }
        } else {
            System.out.println("Disconnected session " + sessionId + " had no active room subscriptions to clean up.");
        }
        System.out.println("User " + disconnectedUserId + " fully disconnected.");
    }

    /**
     * Gets the current set of users online in a specific chatroom.
     *
     * @param chatroomId The unique ID of the chatroom.
     * @return A set of OnlineUserDTOs for users in that room, or an empty set if no one is online.
     */
    public Set<OnlineUserDTO> getOnlineUsersInRoom(UUID chatroomId) {
        return onlineUsersInRooms.getOrDefault(chatroomId, Collections.emptySet());
    }

    /**
     * Sends the current list of online users for a chatroom to its dedicated WebSocket topic.
     * This updates all connected clients subscribed to that room's online user updates.
     *
     * @param chatroomId The unique ID of the chatroom.
     */
    public void broadcastOnlineUsers(UUID chatroomId) {
        Set<OnlineUserDTO> currentOnlineUsers = getOnlineUsersInRoom(chatroomId);
        // Convert the Set to a List for consistent JSON array representation on the frontend
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/onlineUsers", currentOnlineUsers.stream().collect(Collectors.toList()));
        System.out.println("Broadcasting " + currentOnlineUsers.size() + " online users for room " + chatroomId);
    }
}
