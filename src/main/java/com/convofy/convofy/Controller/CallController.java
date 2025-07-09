package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.MeetSession;
import com.convofy.convofy.Entity.WaitingQueue;
import com.convofy.convofy.Repository.MeetRepository;
import com.convofy.convofy.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/call")
public class CallController {
    @Autowired
    private MeetRepository meetRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private WaitingQueue waitingQueue = WaitingQueue.getInstance();

    @PostMapping("/StartCall")
    public ResponseEntity<Response<String>> Startcall(@RequestBody MeetSession meetSession) throws Exception {
        if (meetSession == null || meetSession.getSessionid() == null || meetSession.getSessionid().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "Invalid meetSession or missing session ID.", null));
        }

        Optional<MeetSession> meetsessOptional = meetRepository.findById(meetSession.getSessionid());

        if (meetsessOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Meeting session not found in database.", null));
        }

        MeetSession existingMeetSession = meetsessOptional.get();

        if (existingMeetSession.getStart_time() != null) {
            System.out.println("Call for session " + existingMeetSession.getSessionid() + " has already started.");
            return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "Call has already begun.", null));
        } else {
            System.out.println("First user joining call for session: " + existingMeetSession.getSessionid());

            existingMeetSession.setStart_time(Time.valueOf(LocalTime.now()));
            existingMeetSession.setStatus(true);

            meetRepository.save(existingMeetSession);

            if (existingMeetSession.getUserid1() != null && !existingMeetSession.getUserid1().isEmpty()) {
                waitingQueue.changeStatus(UUID.fromString(existingMeetSession.getUserid1()), "BUSY");
            }
            if (existingMeetSession.getUserid2() != null && !existingMeetSession.getUserid2().isEmpty()) {
                waitingQueue.changeStatus(UUID.fromString(existingMeetSession.getUserid2()), "BUSY");
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "Call started successfully.", null));
        }
    }


    @PostMapping("/endCall")
    public ResponseEntity<Response<String>> endCall(@RequestBody Map<String, String> payload) {
        String sessionId = payload.get("sessionId");
        String endingUserId = payload.get("userId");

        if (sessionId == null || sessionId.isEmpty() || endingUserId == null || endingUserId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false, "Missing sessionId or userId in request.", null));
        }

        Optional<MeetSession> meetSessionOptional = meetRepository.findById(sessionId);

        if (meetSessionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "Meeting session not found for ID: " + sessionId, null));
        }

        MeetSession meetSession = meetSessionOptional.get();

        meetSession.setEnd_time(Time.valueOf(LocalTime.now()));
        meetSession.setStatus(false);
        meetSession.setEndedByClient(true);
        meetRepository.save(meetSession);

        Map<String, String> endCallPayload = new HashMap<>();
        endCallPayload.put("sessionId", sessionId);
        endCallPayload.put("endedByUserId", endingUserId);

        // Changed to broadcast to /call topic
        messagingTemplate.convertAndSend("/call", endCallPayload);
        System.out.println("Sent call-end notification to /call for session: " + sessionId + " by user: " + endingUserId);

        if (meetSession.getUserid1() != null && !meetSession.getUserid1().isEmpty()) {
            waitingQueue.changeStatus(UUID.fromString(meetSession.getUserid1()), "IDLE");
        }
        if (meetSession.getUserid2() != null && !meetSession.getUserid2().isEmpty()) {
            waitingQueue.changeStatus(UUID.fromString(meetSession.getUserid2()), "IDLE");
        }

        return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "Call ended and notification sent.", null));
    }
}