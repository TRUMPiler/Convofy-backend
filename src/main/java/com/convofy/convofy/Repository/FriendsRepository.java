package com.convofy.convofy.Repository;

import com.convofy.convofy.Entity.Friends;
import com.convofy.convofy.Entity.MeetSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendsRepository extends JpaRepository<Friends, String> {
    Optional<Friends> findByUserIdAndFriendId(String userId, String friendId);

    Iterable<Friends> findByUserIdOrFriendIdAndStatus(String userId, String friendId, String status);
}
