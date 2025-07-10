package com.convofy.convofy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchNotificationDTO {
    private String meetId;
    private String message;
    private String userId; // The recipient user's ID (as String)
    private String partnerId; // The partner's ID (as String)
    private String partnerName;
    private String partnerAvatar;
    private String sessionId;
}