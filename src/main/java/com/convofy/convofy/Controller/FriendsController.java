package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.Friends;
import com.convofy.convofy.Repository.FriendsRepository;
import com.convofy.convofy.dto.FriendRequestDTO;
import com.convofy.convofy.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
public class FriendsController {

    @Autowired
    private FriendsRepository friendsRepository;

    @PostMapping("/add")
    public ResponseEntity<Response<String>> addFriend(@RequestBody FriendRequestDTO request) {
        if (request.getUserId().equals(request.getFriendId())) {
            return ResponseEntity.badRequest().body(new Response<>(false, "You cannot add yourself as a friend", null));
        }

        Optional<Friends> existingRequest = friendsRepository.findByUserIdAndFriendId(request.getUserId(), request.getFriendId());
        if (existingRequest.isPresent()) {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request already exists", null));
        }

        Friends friendRequest = new Friends(request.getUserId(), request.getFriendId(), "pending");
        friendsRepository.save(friendRequest);
        return ResponseEntity.ok(new Response<>(true, "Friend request sent", null));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response<String>> updateFriendStatus(@PathVariable UUID id, @RequestParam String status) {

        Optional<Friends> optionalFriend = friendsRepository.findById(id); // Pass UUID directly
        if (optionalFriend.isPresent()) {
            Friends friend = optionalFriend.get();
            friend.setStatus(status);
            friendsRepository.save(friend);
            return ResponseEntity.ok(new Response<>(true, "Friend request updated", null));
        } else {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request not found", null));
        }
    }


    @GetMapping("/list")
    public ResponseEntity<Response<Iterable<Friends>>> getFriendsList(@RequestParam UUID userId) {

        Iterable<Friends> friendsList = friendsRepository.findByUserIdOrFriendIdAndStatus(userId, userId, "accepted");
        return ResponseEntity.ok(new Response<>(true, "Friends list retrieved", friendsList));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response<String>> deleteFriend(@PathVariable UUID id) {

        Optional<Friends> optionalFriend = friendsRepository.findById(id);
        if (optionalFriend.isPresent()) {
            friendsRepository.deleteById(id);
            return ResponseEntity.ok(new Response<>(true, "Friend request or friendship deleted", null));
        } else {
            return ResponseEntity.badRequest().body(new Response<>(false, "Friend request not found", null));
        }
    }
}
