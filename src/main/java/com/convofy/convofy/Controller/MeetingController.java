package com.convofy.convofy.Controller;
import com.convofy.convofy.utils.Response;
import com.convofy.convofy.utils.VideoSDKServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private VideoSDKServices videoSDKService;

    @PostMapping("/create")
    public ResponseEntity<Response<String>> createMeeting() {
        String meetingId = videoSDKService.createMeeting();
        if (meetingId != null) {

            return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new Response<>(true, "MeetId Created Successfully",meetingId));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(false,"Failed to create meeting",null));
        }
    }
}
