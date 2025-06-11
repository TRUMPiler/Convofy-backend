package com.convofy.convofy.CassandraOperations;
import com.convofy.convofy.Entity.User;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CassandraRepository<User, String> {
}

