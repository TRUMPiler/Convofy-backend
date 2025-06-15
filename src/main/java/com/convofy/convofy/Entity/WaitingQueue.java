package com.convofy.convofy.Entity;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Random;
import java.util.random.RandomGenerator;

@Getter
@Setter
public class WaitingQueue {
    private static WaitingQueue instance; // Static instance of the class
    private HashSet<String> users;
    private String randomuserid;

    // Private constructor to prevent external instantiation
    private WaitingQueue() {
        users = new HashSet<>();
    }

    // Static method to get the single instance (Thread-safe using synchronized)
    public static synchronized WaitingQueue getInstance() {
        if (instance == null) {
            instance = new WaitingQueue();
        }
        return instance;
    }

    public int count() {
        return users.size();
    }

    public boolean addUser(String userId) {
        return users.add(userId);
    }
    public String removeUser() {
        if (count() > 1) {
            // Get a random user ID from the HashSet
            String randomUserId = users.stream()
                    .skip(new Random().nextInt(users.size()))
                    .findFirst()
                    .orElse(null);


            users.remove(randomUserId);
            return randomUserId;
        } else {

            return "Not enough users to remove.";
        }
    }

}
