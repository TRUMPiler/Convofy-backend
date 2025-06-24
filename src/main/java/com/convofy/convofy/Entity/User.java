// src/main/java/com/convofy/convofy/Entity/User.java
package com.convofy.convofy.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "app_user") // Renamed from "user" to "app_user" to avoid SQL keyword collision
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Automatically generate UUID
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId; // Changed to UUID type

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password_hash", nullable = false) // Stores hashed password
    private String password; // Changed name for clarity (password vs. hash)

    @Column(name = "phone")
    private String phone; // Nullable by default

    @Column(name = "profile_image_url", columnDefinition = "TEXT") // TEXT for potentially long URLs
    private String image; // Renamed for clarity

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth; // Nullable by default

    @Column(name = "online_status", nullable = false)
    private boolean onlineStatus = false; // Default to false

    @CreationTimestamp // Hibernate annotation for creation timestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt; // Using Instant for precise timestamps

    @UpdateTimestamp // Hibernate annotation for update timestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt; // Using Instant for precise timestamps

    // Many-to-Many relationship with interests
    @ManyToMany(fetch = FetchType.LAZY) // Use LAZY fetch to avoid loading all interests every time
    @JoinTable(
            name = "user_interests", // Name of the junction table
            joinColumns = @JoinColumn(name = "user_id"), // Column in user_interests that refers to this entity's PK
            inverseJoinColumns = @JoinColumn(name = "interest_id") // Column in user_interests that refers to the other entity's PK
    )
    private Set<Interest> interests = new HashSet<>(); // Use Set to ensure uniqueness and efficient operations

    // Helper method to add an interest
    public void addInterest(Interest interest) {
        this.interests.add(interest);
    }

    // Helper method to remove an interest
    public void removeInterest(Interest interest) {
        this.interests.remove(interest);
    }

    // --- IMPORTANT: Override equals() and hashCode() for proper Set behavior ---
    // If you plan to put User objects into Sets or Maps, or manage relationships
    // correctly, these are essential. They should use the unique identifier.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }
}