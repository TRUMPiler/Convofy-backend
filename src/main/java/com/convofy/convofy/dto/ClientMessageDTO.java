package com.convofy.convofy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

// DTO for incoming chat messages from the frontend
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientMessageDTO {
    private UUID chatroomId;
    private String content;
    // The senderId will be extracted from the authenticated Principal on the backend
}
