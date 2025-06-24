package com.convofy.convofy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

// DTO for a client's request to join a specific chatroom
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {
    private UUID chatroomId;
}
