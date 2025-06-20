package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.MeetSession;
import com.convofy.convofy.Entity.WaitingQueue;
import com.convofy.convofy.utils.MatchNotificationService;
import com.convofy.convofy.utils.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class WaitingQueueController {

    private WaitingQueue waitingQueue=WaitingQueue.getInstance();

    @GetMapping("/check/{userid}")
    public synchronized ResponseEntity<Response<String>>  checkWaitingQueue(@PathVariable String userid) throws Exception {
        if(userid==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<String>(true,"No userid",null));
        }
        if(waitingQueue.count()>0)
        {
            if(waitingQueue.checkid(userid))
            {
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(new Response<String>(true,"Waiting List",null));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response<String>(true,"User Found",waitingQueue.removeid()));
        }
        waitingQueue.addid(userid);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Response<String>(true,"Added in Waiting List",null));
    }

}
