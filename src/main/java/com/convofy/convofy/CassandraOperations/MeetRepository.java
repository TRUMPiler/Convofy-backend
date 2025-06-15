package com.convofy.convofy.CassandraOperations;

import com.convofy.convofy.Entity.MeetSession;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface MeetRepository extends CassandraRepository<MeetSession,String> {

}