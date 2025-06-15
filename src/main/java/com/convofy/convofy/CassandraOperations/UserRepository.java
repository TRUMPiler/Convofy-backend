package com.convofy.convofy.CassandraOperations;
import com.convofy.convofy.Entity.User;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CassandraRepository<User, String>
{
    @Query("SELECT * FROM convofy.user WHERE email = ?0")
    Optional<User> findByEmail(String email);
    @Query("SELECT COUNT(*) FROM convofy.user WHERE email = ?0 ALLOW FILTERING")
    long countByEmail(String email);

}

