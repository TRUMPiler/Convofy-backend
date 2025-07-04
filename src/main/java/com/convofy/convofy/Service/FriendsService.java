package com.convofy.convofy.Service;

import com.convofy.convofy.Entity.Friends;
import com.convofy.convofy.Entity.User;
import com.convofy.convofy.Repository.FriendsRepository;
import com.convofy.convofy.Repository.UserRepository;
import com.convofy.convofy.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FriendsService {

    private final FriendsRepository friendsRepository;
    public UserRepository userRepository;

    @Autowired
    public FriendsService(FriendsRepository friendsRepository,UserRepository userRepository) {
        this.friendsRepository = friendsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Friends SendFriendRequest(UUID userId, UUID friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }
        Optional<Friends> existingFriendship = friendsRepository.findExistingFriendshipOrRequest(userId, friendId);
        if (existingFriendship.isPresent()) {
            Friends friends = existingFriendship.get();
            if ("accepted".equals(friends.getStatus())) {
                throw new IllegalStateException("Friend request is already accepted");
            } else if ("pending".equals(friends.getStatus())) {
                throw new IllegalStateException("Friend request is already pending");
            } else if ("blocked".equals(friends.getStatus())) {
                throw new IllegalStateException("Friend has blocked you");
            }
        }
        Friends friends = new Friends(userId, friendId, "pending");
        return friendsRepository.save(friends);
    }

    @Transactional
    public Friends acceptFriendRequest(UUID requestId, UUID receiverId) {
        Friends request = friendsRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found."));
        if (!request.getFriendId().equals(receiverId)) {
            throw new SecurityException("You are not authorized to accept this request.");
        }
        if ("pending".equals(request.getStatus())) {
            request.setStatus("accepted");
            return friendsRepository.save(request);
        } else {
            throw new IllegalStateException("Cannot accept a request that is not pending.");
        }
    }

    @Transactional
    public void declineFriendRequest(UUID requestId, UUID receiverId) {
        Friends request = friendsRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found."));

        if (!request.getFriendId().equals(receiverId)) {
            throw new SecurityException("You are not authorized to decline this request.");
        }

        if ("pending".equals(request.getStatus())) {
            friendsRepository.delete(request);
        } else {
            throw new IllegalStateException("Cannot decline a request that is not pending.");
        }
    }

    @Transactional
    public void unfriend(UUID user1Id, UUID user2Id) {
        Optional<Friends> relationship = friendsRepository.findExistingFriendshipOrRequest(user1Id, user2Id);
        if (relationship.isPresent() && "accepted".equals(relationship.get().getStatus())) {
            friendsRepository.delete(relationship.get());
        } else {
            throw new IllegalStateException("You are not friends with this user.");
        }
    }

    @Transactional
    public void blockFriend(UUID user1Id, UUID user2Id) {
        Optional<Friends> relationship = friendsRepository.findExistingFriendshipOrRequest(user1Id, user2Id);
        if (relationship.isPresent() && "accepted".equals(relationship.get().getStatus())) {
            friendsRepository.delete(relationship.get());
        } else {
            throw new IllegalStateException("You are not friends with this user. or you have already blocked them");
        }
    }

    public List<IncomingFriendRequestDTO> getPendingIncomingFriendRequests(UUID userId) {
        List<Friends> requests = friendsRepository.findByFriendIdAndStatus(userId, "pending");
        return requests.stream().map(req -> {
            User senderUser = userRepository.findById(req.getUserId()).orElse(null);

            UserBasicInfoDTO senderInfo = (senderUser != null) ?
                    new UserBasicInfoDTO(senderUser.getUserId(), senderUser.getName(), senderUser.getEmail(), senderUser.getImage()) :
                    new UserBasicInfoDTO(req.getUserId(), "Unknown User", "unknown@example.com", "https://ui-avatars.com/api/?name=NA&background=random");
            System.out.println(senderInfo.getUserId());
            return new IncomingFriendRequestDTO(req.getId(), senderInfo, req.getCreatedAt());
        }).collect(Collectors.toList());
    }

    public List<OutgoingFriendRequestDTO> getPendingOutgoingFriendRequests(UUID userId) {
        List<Friends> requests = friendsRepository.findByUserIdAndStatus(userId, "pending");
        return requests.stream().map(req -> {
            User receiverUser = userRepository.findById(req.getFriendId()).orElse(null);
            UserBasicInfoDTO receiverInfo = (receiverUser != null) ?
                    new UserBasicInfoDTO(receiverUser.getUserId(), receiverUser.getName(), receiverUser.getEmail(), receiverUser.getImage()) :
                    new UserBasicInfoDTO(req.getFriendId(), "Unknown User", "unknown@example.com", "https://ui-avatars.com/api/?name=NA&background=random");
            return new OutgoingFriendRequestDTO(req.getId(), receiverInfo, req.getCreatedAt());
        }).collect(Collectors.toList());
    }
    public List<FriendDTO> getFriendsList(UUID userId) {
        List<Friends> acceptedFriendships = friendsRepository.findAllFriendsByUserIdAndStatus(userId, "accepted");

        List<FriendDTO> friendsList = new ArrayList<>();

        for (Friends friendship : acceptedFriendships) {
            UUID friendId;
            if (friendship.getUserId().equals(userId)) {
                friendId = friendship.getFriendId();
            } else {
                friendId = friendship.getUserId();
            }

            Optional<User> friendUserOptional = userRepository.findById(friendId);

            if (friendUserOptional.isPresent()) {
                User friendUser = friendUserOptional.get();
                UserBasicInfoDTO friendInfo = new UserBasicInfoDTO(
                        friendUser.getUserId(),
                        friendUser.getName(),
                        friendUser.getEmail(),
                        friendUser.getImage()
                );
                friendsList.add(new FriendDTO(friendship.getId(), friendInfo));
            } else {
                friendsList.add(new FriendDTO(friendship.getId(),
                        new UserBasicInfoDTO(friendId, "Deleted User", "deleted@example.com", "https://ui-avatars.com/api/?name=DU&background=random")));
            }
        }
        return friendsList;
    }
}
