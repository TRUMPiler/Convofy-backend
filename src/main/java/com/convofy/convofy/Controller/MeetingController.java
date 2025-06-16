package com.convofy.convofy.Controller;
import com.convofy.convofy.CassandraOperations.MeetRepository;
import com.convofy.convofy.Entity.MeetSession;
import com.convofy.convofy.Entity.User;
import com.convofy.convofy.utils.Response;
import com.convofy.convofy.utils.VideoSDKServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private VideoSDKServices videoSDKService;
    private ArrayList<String> meetids = new ArrayList<>();
    @Autowired
    private MeetRepository meetRepository;
    @PostMapping("/create")
    public ResponseEntity<Response<MeetSession>> createMeeting(@RequestBody MeetSession meetSession) throws Exception {

        String meetingId = videoSDKService.createMeeting();

        if (meetingId != null)
        {
            if(meetids.contains(meetingId)){
                meetingId= videoSDKService.createMeeting();
            }
            meetSession.setSessionid(UUID.randomUUID().toString());
            meetSession.setMeetid(meetingId);
            meetids.add(meetingId);
            meetRepository.insert(meetSession);
            return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new Response<>(true, "MeetId Created Successfully",meetSession));
        }
        else
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(false,"Failed to create meeting",null));
        }

    }



    @GetMapping("/exitcall")
    public ResponseEntity<Response<String>> exitcall(@RequestBody MeetSession meetSession) throws Exception {
        meetids.remove(meetSession.getMeetid());

        meetRepository.save(meetSession);
        return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true,"meeting session deactivated",null));
    }
}
