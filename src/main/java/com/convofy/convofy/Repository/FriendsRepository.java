package com.convofy.convofy.Repository;

import com.convofy.convofy.Entity.Friends;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendsRepository extends JpaRepository<Friends, UUID> {
    @Query("SELECT f FROM Friends f WHERE " +
            "(f.userId = :user1Id AND f.friendId = :user2Id) OR " +
            "(f.userId = :user2Id AND f.friendId = :user1Id)")
    Optional<Friends> findExistingFriendshipOrRequest(
            @Param("user1Id") UUID user1Id,
            @Param("user2Id") UUID user2Id
    );

    Optional<Friends> findByUserIdAndFriendId(UUID userId, UUID friendId);
    List<Friends> findByFriendIdAndStatus(UUID friendId, String status);
    List<Friends> findByUserIdAndStatus(UUID userId, String status);
    @Query("SELECT f FROM Friends f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = :status")
    List<Friends> findAllFriendsByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    Iterable<Friends> findByUserIdOrFriendIdAndStatus(UUID userId1, UUID userId2, String status);

    Optional<Friends> findById(UUID id);
    void deleteById(UUID id);
}
