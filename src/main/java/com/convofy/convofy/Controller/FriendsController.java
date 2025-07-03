package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.Friends;
import com.convofy.convofy.Repository.FriendsRepository;
import com.convofy.convofy.Service.FriendsService;
import com.convofy.convofy.dto.FriendRequestDTO;
import com.convofy.convofy.dto.IncomingFriendRequestDTO;
import com.convofy.convofy.dto.OutgoingFriendRequestDTO;
import com.convofy.convofy.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
public class FriendsController {
    private final FriendsService friendsService;
    private final FriendsRepository friendsRepository;

    @Autowired
    public FriendsController(FriendsService friendsService, FriendsRepository friendsRepository) {
        this.friendsService = friendsService;
        this.friendsRepository = friendsRepository;
    }
    @PostMapping("/add")
    public ResponseEntity<Response<String>> addFriends(@RequestBody FriendRequestDTO friendRequestDTO) {
        try{
            Friends ADDorUPDATE=friendsService.SendFriendRequest(friendRequestDTO.getUserId(), friendRequestDTO.getFriendId());
            if("accepted".equals(ADDorUPDATE.getStatus()))
            {
                return ResponseEntity.status(HttpStatus.OK).body(new Response<String>(true,"Friend Request is already Accepted",null));
            }
            else
            {
                return ResponseEntity.status(HttpStatus.OK).body(new Response<String>(true,"Friend Request Sent",null));
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new Response<String>(false,e.getMessage(),null));
        } catch (Exception e){
          System.out.println(e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace()));
          return ResponseEntity.badRequest().body(new Response<String>(false ,"Some other error occured",null));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response<String>> updateFriends(@PathVariable UUID id, @RequestParam UUID currentuserid) {
        try{
            friendsService.acceptFriendRequest(id,currentuserid);
            return ResponseEntity.badRequest().body(new Response<>(true,"Friend Request is already Accepted",null));
        }catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new Response<>(false,e.getMessage(),null));
        }
        catch (Exception e){
            System.out.println(e.getMessage()+"\n"+ Arrays.toString(e.getStackTrace()));
            return ResponseEntity.badRequest().body(new Response<>(false,"Another Error Occurred",null));
        }
    }
    @GetMapping("/list/{userId}")
    public ResponseEntity<Response<List<Friends>>> getFriendsList(@PathVariable UUID userId) {
        List<Friends> friendsList = friendsRepository.findAllFriendsByUserIdAndStatus(userId, "accepted");
        return ResponseEntity.ok(new Response<>(true, "Friends list retrieved successfully", friendsList));
    }

    @DeleteMapping("/unfriend")
    public ResponseEntity<Response<String>> deleteFriends(@RequestBody FriendRequestDTO friendRequestDTO) {
        try{
           friendsService.unfriend(friendRequestDTO.getUserId(), friendRequestDTO.getFriendId() );
           return ResponseEntity.ok(new Response<>(true,"Friend has been unfriended",null));
        }catch (IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.badRequest().body(new Response<String>(false,e.getMessage(),null));
        } catch (Exception e){
            System.out.println(e.getMessage()+"\n"+e.getStackTrace());
            return ResponseEntity.badRequest().body(new Response<String>(false,e.getMessage(),null));
        }
    }
    @GetMapping("/requests/incoming")
    public ResponseEntity<Response<List<IncomingFriendRequestDTO>>> getIncomingFriendRequests(@RequestParam UUID userId) {
        try {
            List<IncomingFriendRequestDTO> requests = friendsService.getPendingIncomingFriendRequests(userId);
            return ResponseEntity.ok(new Response<>(true, "Incoming friend requests retrieved", requests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(false, "Failed to retrieve incoming requests: " + e.getMessage(), null));
        }
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<Response<List<OutgoingFriendRequestDTO>>> getOutgoingFriendRequests(@RequestParam UUID userId) {
        try {
            List<OutgoingFriendRequestDTO> requests = friendsService.getPendingOutgoingFriendRequests(userId);
            return ResponseEntity.ok(new Response<>(true, "Outgoing friend requests retrieved", requests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(false, "Failed to retrieve outgoing requests: " + e.getMessage(), null));
        }
    }
}
