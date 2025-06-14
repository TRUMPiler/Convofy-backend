package com.convofy.convofy.utils;

import com.google.gson.JsonObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.convofy.convofy.utils.Response;
@Service
public class VideoSDKServices {

    private final String API_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGlrZXkiOiIxMmRhZDAzYS1mZTYwLTQzNjgtODI1MS0wYWVhZWU1MWNlOTkiLCJwZXJtaXNzaW9ucyI6WyJhbGxvd19qb2luIl0sImlhdCI6MTc0OTcyOTEzNiwiZXhwIjoxNzgxMjY1MTM2fQ.Da75q68UMwTo325_PgD74qqYj3Ah2Per--IQdU4chNg"; // Replace with your token
    private final String BASE_URL = "https://api.videosdk.live/v2";

    public String createMeeting() {
        try {
            String url = BASE_URL + "/rooms";

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", API_TOKEN); // Pass token as in JavaScript example

            // No body required for this API call
            HttpEntity<String> request = new HttpEntity<>(headers);

            // Make API call
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // Parse the response to extract roomId
                String responseBody = response.getBody();
                JsonObject responseObject = new com.google.gson.JsonParser().parse(responseBody).getAsJsonObject();
                return responseObject.get("roomId").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
