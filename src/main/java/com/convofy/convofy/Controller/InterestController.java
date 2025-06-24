package com.convofy.convofy.Controller;

import com.convofy.convofy.Entity.Interest;
import com.convofy.convofy.Repository.InterestRepository;
import com.convofy.convofy.utils.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interests")
public class InterestController {

    @Autowired
    private InterestRepository interestRepository;

    @GetMapping
    public ResponseEntity<Response<List<Interest>>> getAllInterests(@RequestParam(required = false) String status) {
        try {
            List<Interest> interests;

            if (status != null && !status.trim().isEmpty()) {
                try {
                    System.out.println(status);
                    Interest.InterestStatus interestStatus = Interest.InterestStatus.valueOf(status.toUpperCase());
                    interests = interestRepository.findByStatus(interestStatus);

                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new Response<>(false, "Invalid status parameter. Must be ACTIVE or INACTIVE.", null));
                }
            } else {
                interests = interestRepository.findAll();
                System.out.println(interests.size());
            }
            return ResponseEntity.ok(new Response<>(true, "Interests retrieved successfully", interests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to retrieve interests: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<Interest>> getInterestById(@PathVariable UUID id) {
        try {
            Optional<Interest> interest = interestRepository.findById(id);
            return interest.map(value -> ResponseEntity.ok(new Response<>(true, "Interest found", value)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new Response<>(false, "Interest not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to retrieve interest: " + e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<Response<Interest>> createInterest(@RequestBody Interest interest) {
        try {
            if (interestRepository.findByName(interest.getName()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>(false, "Interest with this name already exists", null));
            }
            Interest savedInterest = interestRepository.save(interest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(true, "Interest created successfully", savedInterest));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to create interest: " + e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Response<Interest>> updateInterest(@PathVariable UUID id, @RequestBody Interest interestUpdates) {
        try {
            Optional<Interest> interestOptional = interestRepository.findById(id);
            if (interestOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>(false, "Interest not found", null));
            }

            Interest existingInterest = interestOptional.get();

            if (interestUpdates.getName() != null && !interestUpdates.getName().trim().isEmpty()) {
                Optional<Interest> existingNameConflict = interestRepository.findByName(interestUpdates.getName());
                if (existingNameConflict.isPresent() && !existingNameConflict.get().getInterestId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new Response<>(false, "Interest with this new name already exists", null));
                }
                existingInterest.setName(interestUpdates.getName());
            }

            if (interestUpdates.getStatus() != null) {
                existingInterest.setStatus(interestUpdates.getStatus());
            }

            Interest updatedInterest = interestRepository.save(existingInterest);
            return ResponseEntity.ok(new Response<>(true, "Interest updated successfully", updatedInterest));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to update interest: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<String>> deleteInterest(@PathVariable UUID id) {
        try {
            if (!interestRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>(false, "Interest not found", null));
            }
            interestRepository.deleteById(id);
            return ResponseEntity.ok(new Response<>(true, "Interest deleted successfully", "Interest ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to delete interest: " + e.getMessage(), null));
        }
    }
}
