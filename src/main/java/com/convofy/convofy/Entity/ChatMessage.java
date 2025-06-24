package com.convofy.convofy.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // For UUID generation if it's the primary key
    private UUID messageId;

    @Column(nullable = false)
    private UUID chatroomId; // Stored as UUID

    @Column(nullable = false)
    private UUID senderId;   // Stored as UUID

    @Column(nullable = false, length = 1000) // Example length
    private String content;

    @Column(nullable = false)
    private Instant timestamp;
}