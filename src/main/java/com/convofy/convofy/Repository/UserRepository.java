package com.convofy.convofy.Repository; // Consider renaming package from CassandraOperations
// to something like 'Repository' or 'PostgresRepository'

import com.convofy.convofy.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository; // Changed from CassandraRepository
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>
{
    // Spring Data JPA can automatically derive this query from the method name.
    // The 'email' field is the primary key, so findById(email) would also work.
    Optional<User> findByEmail(String email);

    // Spring Data JPA can automatically derive the count query.
    // countByEmail(String email) would typically be for counting users where a non-primary key field matches.
    // For counting if a user with a specific email (PK) exists, existsById(email) is more idiomatic.
    // If you need a custom count based on email for some other reason, this method signature is fine,
    // but the implementation is handled by JpaRepository, no @Query needed.
    long countByEmail(String email);

    // More idiomatic way to check if a user with a given email exists (since email is PK)
    boolean existsByEmail(String email);
}