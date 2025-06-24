package com.convofy.convofy.Controller;



import com.convofy.convofy.Entity.Friends;
import com.convofy.convofy.Repository.FriendsRepository;
import com.convofy.convofy.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/friends")
public class FriendsController {

    @Autowired
    private FriendsRepository friendsRepository;

    // Create a new friend request
    @PostMapping("/add")
    public ResponseEntity<Response<String>> addFriend(@RequestParam String userId, @RequestParam String friendId) {
        if (userId.equals(friendId)) {
            return ResponseEntity.badRequest().body(new Response<>(false, "You cannot add yourself as a friend", null));
        }

        // Check if the friend request already exists
        Optional<Friends> existingRequest = friendsRepository.findByUserIdAndFriendId(userId, friendId);
        if (existingRequest.isPresent()) {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request already exists", null));
        }

        Friends friendRequest = new Friends(userId, friendId, "pending");
        friendsRepository.save(friendRequest);
        return ResponseEntity.ok(new Response<>(true, "Friend request sent", null));
    }

    // Update the status of a friend request
    @PutMapping("/update")
    public ResponseEntity<Response<String>> updateFriendStatus(@RequestParam int id, @RequestParam String status) {
        Optional<Friends> optionalFriend = friendsRepository.findById(String.valueOf(id));
        if (optionalFriend.isPresent()) {
            Friends friend = optionalFriend.get();
            friend.setStatus(status);
            friendsRepository.save(friend);
            return ResponseEntity.ok(new Response<>(true, "Friend request updated", null));
        } else {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request not found", null));
        }
    }

    // Retrieve all friends for a specific user
    @GetMapping("/list")
    public ResponseEntity<Response<Iterable<Friends>>> getFriendsList(@RequestParam String userId) {
        Iterable<Friends> friendsList = friendsRepository.findByUserIdOrFriendIdAndStatus(userId, userId, "accepted");
        return ResponseEntity.ok(new Response<>(true, "Friends list retrieved", friendsList));
    }

    // Delete a friend or cancel a friend request
    @DeleteMapping("/delete")
    public ResponseEntity<Response<String>> deleteFriend(@RequestParam int id) {
        Optional<Friends> optionalFriend = friendsRepository.findById(String.valueOf(id));
        if (optionalFriend.isPresent()) {
            friendsRepository.deleteById(String.valueOf(id));
            return ResponseEntity.ok(new Response<>(true, "Friend request or friendship deleted", null));
        } else {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request not found", null));
        }
    }
}
