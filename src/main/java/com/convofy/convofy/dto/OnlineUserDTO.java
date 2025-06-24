package com.convofy.convofy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

// DTO for representing an online user in a chatroom for the frontend
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserDTO {
    private UUID userId;
    private String name;
    private String email;
    private String avatar; // URL to the user's avatar
}
