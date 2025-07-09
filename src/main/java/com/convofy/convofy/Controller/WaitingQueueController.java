package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.WaitingQueue;
import com.convofy.convofy.dto.WaitingQueueInterestDTO;
import com.convofy.convofy.utils.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/queue")
public class WaitingQueueController {

    private WaitingQueue waitingQueue = WaitingQueue.getInstance();

    @GetMapping("/check/{userid}")
    public synchronized ResponseEntity<Response<String>> checkWaitingQueue(@PathVariable String userid) throws Exception {
        if (userid == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<String>(true, "No userid", null));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<String>(false, "This endpoint is deprecated for interest-based matching.", null));
    }


    @PostMapping("/check")
    public synchronized ResponseEntity<Response<String>> checkingforusers(@RequestBody WaitingQueueInterestDTO waitingUserDto) {
        if (waitingUserDto == null || waitingUserDto.userid == null || waitingUserDto.interestid == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "User ID and Interest ID must be provided.", null));
        }

        UUID currentUserId;
        UUID interestId;
        try {
            currentUserId = UUID.fromString(waitingUserDto.userid);
            interestId = UUID.fromString(waitingUserDto.interestid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "Invalid UUID format for User ID or Interest ID.", null));
        }

        String userStatusInQueue = waitingQueue.getuserstatus(currentUserId);

        if (userStatusInQueue != null) {
            if ("IDLE".equals(userStatusInQueue)) {
                System.out.println("User " + currentUserId + " is already IDLE in a queue.");

                UUID foundPartnerId = waitingQueue.findIdlePartner(interestId, currentUserId);
                if (foundPartnerId != null) {


                    return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "Matched with partner!", foundPartnerId.toString()));
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "You are already in queue, waiting for a partner.", null));
                }
            } else if ("BUSY".equals(userStatusInQueue)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Response<>(false, "You are currently in a call/busy. Please end your current call first.", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(false, "User status can't be found or is unknown.", null));
            }
        } else {
            waitingQueue.AddUserToQueue(interestId, currentUserId);
            System.out.println("User " + currentUserId + " added to queue for interest " + interestId);

            UUID foundPartnerId = waitingQueue.findIdlePartner(interestId, currentUserId);

            if (foundPartnerId != null) {
//                waitingQueue.changeStatus(currentUserId, "BUSY");
                // No notification command yet, just retrieve and give userid
                return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "Matched with partner!", foundPartnerId.toString()));
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(true, "Added to waiting list. Waiting for a partner.", null));
            }
        }
    }


    @GetMapping("/leave/{userid}")
    public synchronized ResponseEntity<Response<String>> leavingqueue(@PathVariable String userid) {
        System.out.println("User " + userid + " is leaving queue.");
        if (userid == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "User ID must be provided.", null));
        }

        UUID userId;
        try {
            userId = UUID.fromString(userid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "Invalid UUID format for User ID.", null));
        }

        if (waitingQueue.checkuserinqueue(userId)) {
            if (waitingQueue.removeuserfromqueue(userId)) {

                return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "User removed from queue.", null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(false, "Failed to remove user from queue unexpectedly.", null));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "User not found in any queue.", null));
        }
    }
}