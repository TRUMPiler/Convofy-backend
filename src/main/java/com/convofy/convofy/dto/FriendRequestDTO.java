package com.convofy.convofy.dto;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FriendRequestDTO {
    private UUID id;
    private UUID userId;
    private UUID friendId;
    private String Status="pending";
}
