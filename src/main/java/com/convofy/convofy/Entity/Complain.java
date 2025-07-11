package com.convofy.convofy.Entity;

import com.convofy.convofy.dto.ComplainDTO;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "complain")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Complain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Complain(String subject, String description, String imageUrl, UUID userId) {
        this.subject = subject;
        this.description = description;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    public Complain(ComplainDTO dto) {
        this.subject = dto.subject;
        this.description = dto.description;
        this.imageUrl = dto.image;
        this.userId = UUID.fromString(dto.userid);
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
