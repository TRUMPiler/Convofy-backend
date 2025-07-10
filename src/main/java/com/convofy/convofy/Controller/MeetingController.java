package com.convofy.convofy.Controller;
import com.convofy.convofy.Entity.User;
import com.convofy.convofy.Entity.MeetSession;
import com.convofy.convofy.Repository.MeetRepository;
import com.convofy.convofy.Repository.UserRepository;
import com.convofy.convofy.dto.MatchNotificationDTO;
import com.convofy.convofy.dto.UserBasicInfoDTO;
import com.convofy.convofy.utils.Response;
import com.convofy.convofy.utils.VideoSDKServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
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
    public synchronized ResponseEntity<Response<MatchNotificationDTO>> createMeeting(@RequestBody MeetSession meetSession) throws Exception {
        if(meetSession==null)
        {
            System.err.println("meetSession is null");
            // Always return a ResponseEntity
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false,"Meeting session data is null",null));
        }
        if(meetSession.getUserid1()==null||meetSession.getUserid2()==null)
        {
            System.err.println("meetSession.getUserid1 and meetSession.getUserid2 is null");
            // Always return a ResponseEntity
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>(false,"Both user IDs are required",null));
        }
        String meetingId = CreateMeetid();

        if (meetingId != null) {
            // Initialize WebSocketController if it's not already injected/managed by Spring
            if (webSocketController == null) { // Added null check for robustness
                webSocketController = new WebSocketController(this.simpMessagingTemplate);
            }
            meetSession.setMeetid(meetingId);
            meetSession.setSessionid(UUID.randomUUID().toString());
            meetRepository.save(meetSession);

            // Fetch user details for both participants
            Optional<User> user1Optional = userRepository.findById(UUID.fromString(meetSession.getUserid1()));
            Optional<User> user2Optional = userRepository.findById(UUID.fromString(meetSession.getUserid2()));

            if (user1Optional.isPresent() && user2Optional.isPresent()) {
                User user1 = user1Optional.get(); // Meet creator
                User user2 = user2Optional.get(); // Waiting person

                // Create UserBasicInfoDTOs for each user
                UserBasicInfoDTO user1Info = new UserBasicInfoDTO(user1.getUserId(), user1.getName(), user1.getEmail(), user1.getImage());
                UserBasicInfoDTO user2Info = new UserBasicInfoDTO(user2.getUserId(), user2.getName(), user2.getEmail(), user2.getImage());

                // Prepare MatchNotificationDTO for the meet creator (user1) for HTTP response
                MatchNotificationDTO matchNotificationDTOForCreator = new MatchNotificationDTO(
                        meetSession.getMeetid(),
                        "You have been matched!",
                        user1Info.getUserId().toString(), // Recipient: User1
                        user2Info.getUserId().toString(), // Partner: User2
                        user2Info.getName(),
                        user2Info.getAvatar(),
                        meetSession.getSessionid()
                );

               try{
                    Thread.sleep(2000);
               }catch(Exception e)
               {
                   System.err.println(e);
               }
                webSocketController.notifyUser(
                        user2.getUserId().toString(), // Recipient: User2
                        user1Info,                     // Partner for User2 is User1
                        meetSession.getMeetid(),
                        meetSession.getSessionid()
                );

                // Return MatchNotificationDTO to the meet creator (user1) via HTTP response
                return ResponseEntity.status(HttpStatus.CREATED).body(new Response<>(true, "MeetId Created Successfully", matchNotificationDTOForCreator));
            } else {
                System.err.println("One or both users not found when creating meeting. User1 ID: " + meetSession.getUserid1() + ", User2 ID: " + meetSession.getUserid2());
                // If users not found, return an appropriate error response
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "One or both users not found.", null));
            }
        } else {
            // If meetingId could not be created
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to create meeting ID", null));
        }
    }

    public boolean exitCall(MeetSession meetSession) { // Removed throws Exception as it's not needed here
        if(meetSession.getUserid1()==null||meetSession.getUserid2()==null||meetSession.getSessionid()==null)
        {
            System.err.println("meetSession.getUserid1, meetSession.getUserid2, or meetSession.getSessionid is null for exitCall");
            return false;
        }
        meetSession.setStatus(false);
        meetSession.setEnd_time(Time.valueOf(LocalTime.now()));
        meetRepository.save(meetSession);
        return true;
    }
}