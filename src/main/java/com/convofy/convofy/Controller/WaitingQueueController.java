package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.User;
import com.convofy.convofy.Entity.WaitingQueue;
import com.convofy.convofy.utils.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
public class WaitingQueueController {
    private WaitingQueue waitingQueue = WaitingQueue.getInstance();

    @GetMapping("/check")
    public ResponseEntity<Response<String>> CheckWaitingQueue(@RequestBody User user) throws Exception {
        if (waitingQueue.count() > 0) {
            return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "Waiting Queue not empty", waitingQueue.removeUser()));
        }
        waitingQueue.addUser(user.getUserid());
        return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(true, "Waiting Queue empty", null));
    }
}
