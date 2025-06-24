package com.convofy.convofy.Controller;

import com.convofy.convofy.Repository.UserRepository;
import com.convofy.convofy.Entity.User;
import com.convofy.convofy.utils.LoginRequest;
import com.convofy.convofy.utils.Response;
import com.convofy.convofy.utils.PasswordHasher; // Keep this
import com.convofy.convofy.utils.JwtUtil; // Import JwtUtil
import com.convofy.convofy.utils.LoginResponse; // Import LoginResponse

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil; // Inject JwtUtil

    @PostMapping
    public ResponseEntity<Response<LoginResponse>> createUser(@RequestBody User user) { // Changed return type
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>(false, "Email already exists", null));
            }

            PasswordHasher passwordHasher = new PasswordHasher();
            user.setPassword(passwordHasher.hashPassword(user.getPassword()));

            User savedUser = userRepository.save(user); // Save to get the generated userId

            // Generate JWT for the newly created user using their email as subject
            String jwt = jwtUtil.generateToken(savedUser.getEmail());

            LoginResponse loginResponse = new LoginResponse(
                    jwt,
                    savedUser.getUserId(),
                    savedUser.getEmail(),
                    savedUser.getName(),
                    savedUser.getImage()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(true, "User created successfully and logged in", loginResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to create user: " + e.getMessage(), null));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<Response<User>> getUser(@PathVariable UUID id) {
        try {
            Optional<User> user = userRepository.findById(id);

            return user.map(value -> ResponseEntity.ok(new Response<>(true, "User found", value))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response<>(false, "User not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to retrieve user: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<Response<ArrayList<User>>> getUsers() {
        try {
            ArrayList<User> users = new ArrayList<>(userRepository.findAll());
            return ResponseEntity.ok(new Response<>(true, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to retrieve users: " + e.getMessage(), null));
        }
    }

    @PatchMapping("/user")
    public ResponseEntity<Response<String>> updateUser(@RequestBody User userUpdates) {
        try {
            Optional<User> userOptional = userRepository.findById(userUpdates.getUserId());

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new Response<>(false, "User not found", null));
            }

            User existingUser = userOptional.get();

            if (userUpdates.getName() != null && !userUpdates.getName().isEmpty()) {
                existingUser.setName(userUpdates.getName());
            }
            if (userUpdates.getPassword() != null && !userUpdates.getPassword().isEmpty()) {
                PasswordHasher passwordHasher = new PasswordHasher();
                existingUser.setPassword(passwordHasher.hashPassword(userUpdates.getPassword()));
            }
            if (userUpdates.getEmail() != null && !userUpdates.getEmail().isEmpty()) {
                if (!userUpdates.getEmail().equals(existingUser.getEmail()) && userRepository.existsByEmail(userUpdates.getEmail())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new Response<>(false, "New email already registered by another user", null));
                }
                existingUser.setEmail(userUpdates.getEmail());
            }
            if (userUpdates.getPhone() != null && !userUpdates.getPhone().isEmpty()) {
                existingUser.setPhone(userUpdates.getPhone());
            }
            if (userUpdates.getDateOfBirth() != null) {
                existingUser.setDateOfBirth(userUpdates.getDateOfBirth());
            }
            if (userUpdates.getImage() != null && !userUpdates.getImage().isEmpty()) {
                existingUser.setImage(userUpdates.getImage());
            }

            userRepository.save(existingUser);

            return ResponseEntity.ok(new Response<>(true, "User updated successfully", "User ID: " + existingUser.getUserId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to update user: " + e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> LoginUser(@RequestBody LoginRequest loginRequest) { // Changed return type
        try {
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>(false, "User not found", null));
            }

            User user = userOptional.get();
            PasswordHasher passwordHasher = new PasswordHasher();

            if (!passwordHasher.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new Response<>(false, "Invalid email or password", null));
            }

            // IMPORTANT: For real-time presence (WebSockets), we will rely on WebSocket events
            // and an in-memory map. This DB update for onlineStatus here might become redundant
            // for active chat sessions, but useful for overall user status (e.g., for user profiles).
            user.setOnlineStatus(true);
            userRepository.save(user);

            // Generate JWT for the logged-in user using their email as subject
            String jwt = jwtUtil.generateToken(user.getEmail());

            LoginResponse loginResponse = new LoginResponse(
                    jwt,
                    user.getUserId(),
                    user.getEmail(),
                    user.getName(),
                    user.getImage()
            );

            return ResponseEntity.ok(new Response<>(true, "Login successful", loginResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "An error occurred during login", null));
        }
    }

    @PatchMapping("/status")
    public ResponseEntity<Response<String>> updateUserStatus(@RequestBody User userUpdates) {
        System.out.println("Received status update request for userid: " + userUpdates.getUserId() + ", new status: " + userUpdates.isOnlineStatus());

        try {
            Optional<User> userOptional = userRepository.findById(userUpdates.getUserId());

            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();

                existingUser.setOnlineStatus(userUpdates.isOnlineStatus());

                userRepository.save(existingUser);

                return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "User Status successfully updated", "User ID: " + existingUser.getUserId()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "User not found with provided userId", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to update user status: " + e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Response<String>> logoutUser(@RequestBody User userUpdates) {
        try {
            Optional<User> userOptional = userRepository.findById(userUpdates.getUserId());

            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                existingUser.setOnlineStatus(false);
                userRepository.save(existingUser);
                return ResponseEntity.ok(new Response<>(true, "Logout successful", "User ID: " + existingUser.getUserId()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "User not found with provided userId", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response<>(false, "An error occurred during logout", null));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Response<String>> deleteUser(@PathVariable UUID id) {
        try {
            if(userRepository.findById(id).isEmpty()){
                return ResponseEntity.badRequest().body(new Response<>(false, "User not found", null));
            }
            userRepository.deleteById(id);
            return ResponseEntity.ok(new Response<>(true, "User deleted successfully", "User ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "Failed to delete user: " + e.getMessage(), null));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Response<Long>> userCount() {
        try {
            long count = userRepository.count();
            return ResponseEntity.ok(new Response<>(true, "User count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }
}
