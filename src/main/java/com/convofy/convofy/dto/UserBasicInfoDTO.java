package com.convofy.convofy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicInfoDTO {
    private UUID userId;
    private String name;

    public UserBasicInfoDTO(UUID userId, String unknownUser, String mail, String url) {
    }
}
