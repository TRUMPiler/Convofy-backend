package com.convofy.convofy.Controller;
import com.convofy.convofy.Entity.User;
import com.convofy.convofy.Entity.WaitingQueue;
import com.convofy.convofy.Repository.MeetRepository;
import com.convofy.convofy.Entity.MeetSession;
import com.convofy.convofy.Repository.UserRepository;
import com.convofy.convofy.dto.UserBasicInfoDTO;
import com.convofy.convofy.utils.Response;
import com.convofy.convofy.utils.VideoSDKServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @Autowired // Inject UserRepository to fetch user details
    private UserRepository userRepository;


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

            // Fetch user details for both participants
            Optional<User> user1Optional = userRepository.findById(UUID.fromString(meetSession.getUserid1()));
            Optional<User> user2Optional = userRepository.findById(UUID.fromString(meetSession.getUserid2()));

            if (user1Optional.isPresent() && user2Optional.isPresent()) {
                User user1 = user1Optional.get();
                User user2 = user2Optional.get();

                // Create UserBasicInfoDTOs for each user
                UserBasicInfoDTO user1Info = new UserBasicInfoDTO(user1.getUserId(), user1.getName(), user1.getEmail(), user1.getImage());
                UserBasicInfoDTO user2Info = new UserBasicInfoDTO(user2.getUserId(), user2.getName(), user2.getEmail(), user2.getImage());

                // Notify user1 about user2, and user2 about user1 with rich partner info
                webSocketController.notifyUser(user1.getUserId().toString(), user2Info, meetSession.getMeetid(),meetSession.getSessionid());
                webSocketController.notifyUser(user2.getUserId().toString(), user1Info,  meetSession.getMeetid(),meetSession.getSessionid());

            } else {
                System.err.println("One or both users not found when creating meeting. User1 ID: " + meetSession.getUserid1() + ", User2 ID: " + meetSession.getUserid2());
                // Fallback: Notify with basic info if user details cannot be fetched
                webSocketController.notifyUser(meetSession.getUserid1(), new UserBasicInfoDTO(null, "Unknown User", null, null), meetSession.getMeetid(),meetSession.getSessionid());
                webSocketController.notifyUser(meetSession.getUserid2(), new UserBasicInfoDTO(null, "Unknown User", null, null), meetSession.getMeetid(),meetSession.getSessionid());
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(true, "MeetId Created Successfully", meetSession));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to create meeting", null));
        }
    }

    public boolean exitCall(MeetSession meetSession) throws Exception {
        if(meetSession.getUserid1()==null||meetSession.getUserid2()==null||meetSession.getSessionid()==null)
        {
            System.err.println("meetSession.getUserid1 and meetSession.getUserid2 is null");
            return false;
        }
        meetSession.setStatus(false);
        meetSession.setEnd_time(Time.valueOf(LocalTime.now()));
       meetRepository.save(meetSession);
       return true;
    }
}
