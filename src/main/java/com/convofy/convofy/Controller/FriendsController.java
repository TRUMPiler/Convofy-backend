package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.Friends;
import com.convofy.convofy.Repository.FriendsRepository;
import com.convofy.convofy.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID; // Import UUID

@RestController
@RequestMapping("/api/friends")
public class FriendsController {

    @Autowired
    private FriendsRepository friendsRepository;

    // Create a new friend request
    // Assumes userId and friendId in the request are UUID strings that Spring will convert.
    @PostMapping("/add")
    public ResponseEntity<Response<String>> addFriend(@RequestParam UUID userId, @RequestParam UUID friendId) {
        if (userId.equals(friendId)) {
            return ResponseEntity.badRequest().body(new Response<>(false, "You cannot add yourself as a friend", null));
        }

        // Check if the friend request already exists
        // This requires friendsRepository.findByUserIdAndFriendId to accept UUIDs
        Optional<Friends> existingRequest = friendsRepository.findByUserIdAndFriendId(userId, friendId);
        if (existingRequest.isPresent()) {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request already exists", null));
        }

        // Create new Friends entity. Lombok's @NoArgsConstructor and @AllArgsConstructor
        // or a custom constructor should be present in the Friends entity.
        // Ensure Friends constructor/setters expect UUIDs for userId and friendId.
        Friends friendRequest = new Friends(userId, friendId, "pending");
        friendsRepository.save(friendRequest); // Persists the new friend request
        return ResponseEntity.ok(new Response<>(true, "Friend request sent", null));
    }

    // Update the status of a friend request
    // Uses @PathVariable for the ID, assuming the ID is a UUID string in the URL.
    @PutMapping("/update/{id}")
    public ResponseEntity<Response<String>> updateFriendStatus(@PathVariable UUID id, @RequestParam String status) {
        // Find the friend request by its UUID
        Optional<Friends> optionalFriend = friendsRepository.findById(id); // Pass UUID directly
        if (optionalFriend.isPresent()) {
            Friends friend = optionalFriend.get();
            friend.setStatus(status); // Update the status of the existing entity
            friendsRepository.save(friend); // Save the updated entity
            return ResponseEntity.ok(new Response<>(true, "Friend request updated", null));
        } else {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request not found", null));
        }
    }

    // Retrieve all friends for a specific user
    // Assumes userId in the request is a UUID string.
    @GetMapping("/list")
    public ResponseEntity<Response<Iterable<Friends>>> getFriendsList(@RequestParam UUID userId) {
        // This requires friendsRepository.findByUserIdOrFriendIdAndStatus to accept UUIDs
        // and handle both sides of the friendship.
        Iterable<Friends> friendsList = friendsRepository.findByUserIdOrFriendIdAndStatus(userId, userId, "accepted");
        return ResponseEntity.ok(new Response<>(true, "Friends list retrieved", friendsList));
    }

    // Delete a friend or cancel a friend request
    // Uses @PathVariable for the ID, assuming the ID is a UUID string in the URL.
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response<String>> deleteFriend(@PathVariable UUID id) {
        // Check if the friend request exists before attempting to delete
        Optional<Friends> optionalFriend = friendsRepository.findById(id); // Pass UUID directly
        if (optionalFriend.isPresent()) {
            friendsRepository.deleteById(id); // Delete by UUID
            return ResponseEntity.ok(new Response<>(true, "Friend request or friendship deleted", null));
        } else {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request not found", null));
        }
    }
}
