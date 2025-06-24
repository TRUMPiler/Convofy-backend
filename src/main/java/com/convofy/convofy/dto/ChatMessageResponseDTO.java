package com.convofy.convofy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for chat messages sent from the backend to the frontend
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDTO {
    private String id; // Message ID, as String (UUID will be converted)
    private String userId; // Sender's User ID, as String (UUID will be converted)
    private String userName; // Sender's name
    private String userAvatar; // Sender's avatar URL
    private String text; // Message content
    private String time; // Timestamp, as String (Instant will be converted)
}
