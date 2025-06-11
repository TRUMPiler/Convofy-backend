package com.convofy.convofy.Controller;
import com.convofy.convofy.CassandraOperations.UserRepository;
import com.convofy.convofy.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.convofy.convofy.utils.Response;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @PostMapping
    public ResponseEntity<Response<String>> createUser(@RequestBody User user) throws Exception{
        try {
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(true, "User created successfully", "User ID: " + user.getUserid()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<User>> getUser(@PathVariable String id)throws Exception {
        try {
            Optional<User> user = userRepository.findById(id);
            return user.map(value -> ResponseEntity.ok(new Response<>(true, "User found", value))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response<>(false, "User not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<Response<ArrayList<User>>> getUsers() throws Exception{
        try {
            ArrayList<User> users = new ArrayList<>(userRepository.findAll());
            return ResponseEntity.ok(new Response<>(true, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping
    public ResponseEntity<Response<String>> updateUser(@RequestBody User user) throws Exception{
        try {
            userRepository.save(user);
            return ResponseEntity.ok(new Response<>(true, "User updated successfully", "User ID: " + user.getUserid()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping
    public ResponseEntity<Response<String>> deleteUser(@RequestBody User user) throws Exception{
        try {
            userRepository.delete(user);
            return ResponseEntity.ok(new Response<>(true, "User deleted successfully", "User ID: " + user.getUserid()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Response<Long>> userCount() throws Exception {
        try {
            long count = userRepository.count();
            return ResponseEntity.ok(new Response<>(true, "User count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }
}



