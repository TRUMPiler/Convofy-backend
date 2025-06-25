package com.convofy.convofy.Repository;

import com.convofy.convofy.Entity.Friends;
import com.convofy.convofy.Entity.MeetSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FriendsRepository extends JpaRepository<Friends, String> {
    Optional<Friends> findByUserIdAndFriendId(UUID userId, UUID friendId);

    Iterable<Friends> findByUserIdOrFriendIdAndStatus(UUID userId, UUID friendId, String status);

    Optional<Friends> findById(UUID id);

    void deleteById(UUID id);
}
