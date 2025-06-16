package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.WaitingQueue;
import com.convofy.convofy.utils.MatchNotificationService;
import com.convofy.convofy.utils.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class WaitingQueueController {
    private final MatchNotificationService matchNotificationService;
    private WaitingQueue waitingQueue=WaitingQueue.getInstance();

    public WaitingQueueController(MatchNotificationService matchNotificationService) {
        this.matchNotificationService = matchNotificationService;
    }

    @GetMapping("/check/{userid}")
    public ResponseEntity<Response<String>> checkWaitingQueue(@PathVariable String userid){
        if(waitingQueue.count()>0)
        {
            String id= waitingQueue.removeid();
            if(id.equals(userid))
            {
                waitingQueue.addid(userid);
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(new Response<String>(true,"You are already in waiting queue",null));
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response<String>(true,"Removed id is:"+id,id));
        }
        waitingQueue.addid(userid);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Response<String>(true,"User is added",null));
    }

}
