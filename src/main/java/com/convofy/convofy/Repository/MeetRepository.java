package com.convofy.convofy.Repository;

import com.convofy.convofy.Entity.MeetSession;
import org.springframework.data.jpa.repository.JpaRepository; // Changed from CassandraRepository
import org.springframework.stereotype.Repository; // Added for clarity

@Repository // Good practice to add for clarity
public interface MeetRepository extends JpaRepository<MeetSession, String> {
    // JpaRepository<EntityClass, PrimaryKeyType>
    // In your MeetSession entity, 'sessionid' is the String primary key.

    // Spring Data JPA provides common CRUD operations automatically.
    // You can add custom query methods here if needed, for example:
    // List<MeetSession> findByUserid1(String userid1);
    // List<MeetSession> findByUserid2(String userid2);
    // List<MeetSession> findByMeetid(String meetid);
    // List<MeetSession> findByDateTimeBetween(java.time.ZonedDateTime start, java.time.ZonedDateTime end);
}
