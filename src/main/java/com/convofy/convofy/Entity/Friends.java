package com.convofy.convofy.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Automatically generate primary key
    private int id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "friend_id", nullable = false)
    private String friendId;

    @Column(name = "status", nullable = false)
    private String status = "pending"; // Default value

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Automatically set when the record is created

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now(); // Automatically set when the record is updated

    public Friends(String userId, String friendId, String pending) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = pending;
    }


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

