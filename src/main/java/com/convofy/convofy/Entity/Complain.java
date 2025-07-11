package com.convofy.convofy.Entity;

import com.convofy.convofy.dto.ComplainDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter

@Table(name="complain")
public class Complain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="user_id",nullable = false)
    private UUID userid;
    public Complain() {
        LocalDateTime now = LocalDateTime.now();

    }
    public Complain(String subject, String description, String imageUrl) {
        this.subject = subject;
        this.description = description;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }
    public Complain(ComplainDTO complainDTO) {
        this.subject = complainDTO.subject;
        this.description = complainDTO.description;
        this.imageUrl = complainDTO.image;
        this.userid=UUID.fromString(complainDTO.userid);
        this.createdAt = LocalDateTime.now();
    }
}
