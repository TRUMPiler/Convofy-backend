package com.convofy.convofy.Controller;
import com.convofy.convofy.CassandraOperations.UserRepository;
import com.convofy.convofy.Entity.User;
import com.convofy.convofy.utils.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.convofy.convofy.utils.Response;

import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.Optional;
import com.convofy.convofy.utils.PasswordHasher;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    private Cleaner cleaner=Cleaner.create();
    private Runnable runnable=()->{};
    @PostMapping
    public ResponseEntity<Response<String>> createUser(@RequestBody User user) throws Exception{
        try {
            user.setUserid(UUID.randomUUID().toString());
            PasswordHasher passwordHasher = new PasswordHasher();

            long count = userRepository.countByEmail(user.getEmail());
            if (count > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new Response<>(false, "Email already exists", null));
            }


            user.setPassword(passwordHasher.hashPassword(user.getPassword()));
            userRepository.save(user);

            cleaner.register(passwordHasher,runnable);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response<>(true, "User created successfully", "User ID: " + user.getUserid()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }




    @GetMapping("/{id}")
    public ResponseEntity<Response<User>> getUser(@PathVariable String id)throws Exception {
        try {
            Optional<User> user = userRepository.findById(id);
            cleaner.register(user,runnable);
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
            cleaner.register(users,runnable);
            return ResponseEntity.ok(new Response<>(true, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/user")
    public ResponseEntity<Response<String>> updateUser(@RequestBody User user) throws Exception{
        try {
            if(userRepository.findById(user.getUserid()).isEmpty()){
               return ResponseEntity.badRequest().body(new Response<>(false, "User not found", null));
            }
            User existingdetails=userRepository.findById(user.getUserid()).get();
            cleaner.register(existingdetails,runnable);
            if(!user.getUserid().equals(""))
            {
                existingdetails.setUserid(user.getUserid());
            }
            if(!user.getName().equals(""))
            {
                existingdetails.setName(user.getName());
            }
            if(!user.getPassword().equals(""))
            {

                existingdetails.setPassword(user.getPassword());
            }
            if(!user.getEmail().equals(""))
            {
                existingdetails.setEmail(user.getEmail());
            }
            if(!user.getPhone().equals(""))
            {
                existingdetails.setPhone(user.getPhone());
            }
            if(!user.getDob().equals(""))
            {
                existingdetails.setDob(user.getDob());
            }
            if(!user.getImage().equals(""))
            {
                existingdetails.setImage(user.getImage());
            }
            userRepository.save(existingdetails);

            return ResponseEntity.ok(new Response<>(true, "User updated successfully", "User ID: " + user.getUserid()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Response<User>> LoginUser(@RequestBody LoginRequest loginRequest) throws Exception {
        try {
            // Find the user by email
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
            cleaner.register(userOptional,runnable);
            // Check if the user exists
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>(false, "User not found", null));
            }

            User user = userOptional.get();
            cleaner.register(user,runnable);
            if (!PasswordHasher.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
//                System.out.println(PasswordHasher.hashPassword(loginRequest.getPassword())+" "+user.getPassword());
//                System.out.println(loginRequest.getPassword());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new Response<>(false, "Invalid password", null));
            }


            return ResponseEntity.ok(new Response<>(true, "Login successful",user));

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, "An error occurred during login", null));
        }
    }

    @PatchMapping("/status")
    public ResponseEntity<Response<String>> updateUserStatus(@RequestBody User userid) throws Exception{
        System.out.println(userid.isStatus());

        try{

            if(userRepository.findById(userid.getUserid()).isPresent()){
            Optional<User> userdetails = userRepository.findById(userid.getUserid());
            System.out.println(userid.isStatus());
            cleaner.register(userdetails,runnable);
                User user = userdetails.get();

                user.setStatus(userid.isStatus());
                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.OK).body(new Response<>(true, "User Status successfully updated", "User ID: " + user.getUserid()));
            }else
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(false, "User not found", null));
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>(false, e.getMessage(), null));
        }

    }

    @DeleteMapping
    public ResponseEntity<Response<String>> deleteUser(@RequestBody User user) throws Exception{
        try {
            if(userRepository.findById(user.getUserid()).isEmpty()){
                return ResponseEntity.badRequest().body(new Response<>(false, "User not found", null));
            }
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





