package com.convofy.convofy.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String jwt;
    private UUID userId;
    private String email;
    private String name;
    private String image; // URL to profile image
}
