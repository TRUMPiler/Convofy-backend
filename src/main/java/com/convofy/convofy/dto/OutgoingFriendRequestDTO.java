package com.convofy.convofy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutgoingFriendRequestDTO {
    private UUID requestId;
    private UserBasicInfoDTO receiver;
    private LocalDateTime createdAt;
}