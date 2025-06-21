package com.convofy.convofy.Controller;
import com.convofy.convofy.Repository.MeetRepository;
import com.convofy.convofy.Entity.MeetSession;
import com.convofy.convofy.utils.Response;
import com.convofy.convofy.utils.VideoSDKServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private VideoSDKServices videoSDKService=new  VideoSDKServices();
    @Autowired
    private MeetRepository meetRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    WebSocketController webSocketController;
    private Set<String> ids=Collections.synchronizedSet(new HashSet<>());
    public String CreateMeetid()
    {
        String meetid=videoSDKService.createMeeting();
        if(meetid!=null)
        {
            if(ids.contains(meetid))
            {
                return CreateMeetid();
            }
            ids.add(meetid);

            return meetid;
        }
        return null;
    }


    @PostMapping("/create")
    public ResponseEntity<Response<MeetSession>> createMeeting(@RequestBody MeetSession meetSession) throws Exception {
        if(meetSession==null)
        {
            System.err.println("meetSession is null");
        }
        if(meetSession.getUserid1()==null||meetSession.getUserid2()==null)
        {
            System.err.println("meetSession.getUserid1 and meetSession.getUserid2 is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<MeetSession>(false,"",null));
        }
        String meetingId = CreateMeetid();

        if (meetingId != null) {
            webSocketController=new WebSocketController(this.simpMessagingTemplate);
            meetSession.setMeetid(meetingId);
            meetSession.setSessionid(UUID.randomUUID().toString());
            meetRepository.save(meetSession);
            webSocketController.notifyUser(meetSession.getUserid1(), meetSession.getMeetid());
            webSocketController.notifyUser(meetSession.getUserid2(), meetSession.getMeetid());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(true, "MeetId Created Successfully", meetSession));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to create meeting", null));
        }
    }

    @PostMapping("/exitcall")
    public ResponseEntity<Response<String>> exitCall(@RequestBody MeetSession meetSession) throws Exception {
        meetRepository.save(meetSession);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response<>(true, "Meeting session deactivated", null));
    }
}
