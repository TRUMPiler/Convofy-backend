package com.convofy.convofy.Repository; // Consider renaming package from CassandraOperations
// to something like 'Repository' or 'PostgresRepository'

import com.convofy.convofy.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository; // Changed from CassandraRepository
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findById(UUID id);
}