package com.convofy.convofy.Entity;

import lombok.Getter;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


@Getter
public class WaitingQueue {
    ConcurrentSkipListMap<UUID, Map<UUID,String>> usersinqueue;

    private WaitingQueue()
    {
        usersinqueue=new ConcurrentSkipListMap<>();
        System.out.println("WaitingQueue initialized.");
    }
    public static WaitingQueue instance;
    public static WaitingQueue getInstance()
    {
        if (instance == null) {
            instance = new WaitingQueue();
        }
        return instance;
    }

    public boolean AddUserToQueue(UUID interestId, UUID userId) {
        usersinqueue.computeIfAbsent(interestId, k -> new ConcurrentHashMap<>());

        Map<UUID, String> usersForThisInterest = usersinqueue.get(interestId);

        if (usersForThisInterest.containsKey(userId)) {
            System.out.println("User " + userId + " already in queue for interest " + interestId);
            return false;
        }

        usersForThisInterest.put(userId, "IDLE");
        System.out.println("User " + userId + " added to queue for interest " + interestId + " with status IDLE.");
        return true;
    }

    public boolean checkuserinqueue(UUID userId) {
        if (usersinqueue == null || usersinqueue.isEmpty()) {
            return false;
        }
        for (Map<UUID, String> interestUsers : usersinqueue.values()) {
            if (interestUsers.containsKey(userId)) {
                return true;
            }
        }
        return false;
    }

    public boolean changeStatus(UUID userId, String status) {
        if (usersinqueue == null || usersinqueue.isEmpty()) {
            return false;
        }
        for (Map<UUID, String> interestUsers : usersinqueue.values()) {
            if (interestUsers.containsKey(userId)) {
                interestUsers.put(userId, status);
                System.out.println("User " + userId + " status changed to " + status);
                return true;
            }
        }
        System.out.println("User " + userId + " not found in any queue to change status.");
        return false;
    }

    public boolean removeuserfromqueue(UUID userId) {
        if (usersinqueue == null || usersinqueue.isEmpty()) {
            return false;
        }
        boolean removed = false;
        Iterator<Map.Entry<UUID, Map<UUID, String>>> outerIterator = usersinqueue.entrySet().iterator();
        while (outerIterator.hasNext()) {
            Map.Entry<UUID, Map<UUID, String>> entry = outerIterator.next();
            Map<UUID, String> interestUsers = entry.getValue();
            if (interestUsers.containsKey(userId)) {
                interestUsers.remove(userId);
                removed = true;
                System.out.println("User " + userId + " removed from queue for interest " + entry.getKey());
            }
            if (interestUsers.isEmpty()) {
                outerIterator.remove();
                System.out.println("Removed empty interest: " + entry.getKey());
            }
        }
        return removed;
    }

    public String getuserstatus(UUID userId) {
        if (usersinqueue == null || usersinqueue.isEmpty()) {
            return null;
        }
        for (Map<UUID, String> interestUsers : usersinqueue.values()) {
            if (interestUsers.containsKey(userId)) {
                return interestUsers.get(userId);
            }
        }
        return null;
    }

    public UUID findIdlePartner(UUID interestId, UUID currentUser) {
        Map<UUID, String> usersForInterest = usersinqueue.get(interestId);

        if (usersForInterest == null || usersForInterest.isEmpty()) {
            return null;
        }

        for (Map.Entry<UUID, String> entry : usersForInterest.entrySet()) {
            UUID potentialPartnerId = entry.getKey();
            String status = entry.getValue();

            if (!potentialPartnerId.equals(currentUser) && "IDLE".equals(status)) {
                changeStatus(potentialPartnerId, "BUSY");
                System.out.println("Found IDLE partner " + potentialPartnerId + " for user " + currentUser + " in interest " + interestId + ". Partner status set to BUSY.");
                return potentialPartnerId;
            }
        }
        System.out.println("No IDLE partner found for user " + currentUser + " in interest " + interestId);
        return null;
    }

    public boolean removeemptyinterests() {
        if (usersinqueue == null || usersinqueue.isEmpty()) {
            return false;
        }

        boolean removedAny = false;
        Iterator<Map.Entry<UUID, Map<UUID, String>>> iterator = usersinqueue.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Map<UUID, String>> entry = iterator.next();
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                iterator.remove();
                System.out.println("Removed empty interestId: " + entry.getKey());
                removedAny = true;
            }
        }
        return removedAny;
    }
}