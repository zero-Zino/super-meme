package com.example.demo.service.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.model.Friend;
import com.example.demo.model.User;
import com.example.demo.repository.FriendRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.realtime.NotificationService;

@Service
public class FriendService {
    
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    public FriendService(FriendRepository friendRepository, 
                        UserRepository userRepository, 
                        NotificationService notificationService) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    public List<Map<String, Object>> getUserFriends(Long userId) {
        // Get all accepted friend relationships for this user
        List<Friend> friends = friendRepository.findByUserIdAndStatus(userId, Friend.FriendStatus.ACCEPTED);
        
        // Get all accepted friend relationships where this user is the friend
        List<Friend> friendsInverse = friendRepository.findByFriendIdAndStatus(userId, Friend.FriendStatus.ACCEPTED);
        
        // Combine the lists
        Set<Long> friendIds = new HashSet<>();
        for (Friend friend : friends) {
            friendIds.add(friend.getFriendId());
        }
        for (Friend friend : friendsInverse) {
            friendIds.add(friend.getUserId());
        }
        
        // Get user details for all friends
        List<User> friendUsers = userRepository.findAllById(friendIds);
        Map<Long, User> userMap = friendUsers.stream()
            .collect(Collectors.toMap(User::getId, u -> u));
        
        // Create response
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long friendId : friendIds) {
            User friendUser = userMap.get(friendId);
            if (friendUser == null) continue;
            
            Map<String, Object> friendData = new HashMap<>();
            friendData.put("id", friendUser.getId());
            friendData.put("username", friendUser.getUsername());
            // Add other user details as needed
            
            result.add(friendData);
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getPendingFriendRequests(Long userId) {
        // Get friend requests sent to this user
        List<Friend> pendingRequests = friendRepository.findByFriendIdAndStatus(userId, Friend.FriendStatus.PENDING);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend request : pendingRequests) {
            User requester = userRepository.findById(request.getUserId()).orElse(null);
            if (requester == null) continue;
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("id", request.getId());
            requestData.put("userId", requester.getId());
            requestData.put("username", requester.getUsername());
            requestData.put("requestDate", request.getCreatedAt());
            
            result.add(requestData);
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getSentFriendRequests(Long userId) {
        // Get friend requests sent by this user
        List<Friend> sentRequests = friendRepository.findByUserIdAndStatus(userId, Friend.FriendStatus.PENDING);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend request : sentRequests) {
            User recipient = userRepository.findById(request.getFriendId()).orElse(null);
            if (recipient == null) continue;
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("id", request.getId());
            requestData.put("userId", recipient.getId());
            requestData.put("username", recipient.getUsername());
            requestData.put("requestDate", request.getCreatedAt());
            
            result.add(requestData);
        }
        
        return result;
    }
    
    public Map<String, Object> sendFriendRequest(Long userId, Long friendId) {
        // Validate users exist
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        User friend = userRepository.findById(friendId)
            .orElseThrow(() -> new IllegalArgumentException("Friend user not found"));
        
        // Check if friendship already exists
        Optional<Friend> existingFriendship = friendRepository.findFriendship(userId, friendId);
        if (existingFriendship.isPresent()) {
            Friend friendship = existingFriendship.get();
            if (friendship.getStatus() == Friend.FriendStatus.ACCEPTED) {
                throw new IllegalStateException("Users are already friends");
            } else if (friendship.getStatus() == Friend.FriendStatus.PENDING) {
                if (friendship.getUserId().equals(userId)) {
                    throw new IllegalStateException("Friend request already sent");
                } else {
                    // The other user already sent a request, so accept it
                    friendship.setStatus(Friend.FriendStatus.ACCEPTED);
                    friendship.setLastUpdated(LocalDateTime.now());
                    friendRepository.save(friendship);
                    
                    // Send notification to the original requester
                    notificationService.sendNotification(
                        friendship.getUserId(),
                        "Friend Request Accepted",
                        user.getUsername() + " accepted your friend request!",
                        "friend_request_accepted",
                        Map.of("userId", userId)
                    );
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("status", "accepted");
                    result.put("message", "Accepted existing friend request");
                    return result;
                }
            } else if (friendship.getStatus() == Friend.FriendStatus.BLOCKED) {
                throw new IllegalStateException("Unable to send friend request");
            }
        }
        
        // Create new friend request
        Friend friendRequest = new Friend();
        friendRequest.setUserId(userId);
        friendRequest.setFriendId(friendId);
        friendRequest.setStatus(Friend.FriendStatus.PENDING);
        friendRequest.setCreatedAt(LocalDateTime.now());
        friendRequest.setLastUpdated(LocalDateTime.now());
        
        friendRepository.save(friendRequest);
        
        // Send notification to recipient
        notificationService.sendNotification(
            friendId,
            "New Friend Request",
            user.getUsername() + " sent you a friend request",
            "friend_request",
            Map.of("userId", userId)
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "pending");
        result.put("message", "Friend request sent");
        return result;
    }
    
    public void acceptFriendRequest(Long userId, Long requestId) {
        Friend request = friendRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
        
        // Verify this user is the recipient of the request
        if (!request.getFriendId().equals(userId)) {
            throw new IllegalArgumentException("Cannot accept this friend request");
        }
        
        // Check if the request is pending
        if (request.getStatus() != Friend.FriendStatus.PENDING) {
            throw new IllegalStateException("Friend request is not pending");
        }
        
        // Accept the request
        request.setStatus(Friend.FriendStatus.ACCEPTED);
        request.setLastUpdated(LocalDateTime.now());
        friendRepository.save(request);
        
        // Send notification to requester
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            notificationService.sendNotification(
                request.getUserId(),
                "Friend Request Accepted",
                user.getUsername() + " accepted your friend request!",
                "friend_request_accepted",
                Map.of("userId", userId)
            );
        }
    }
    
    public void rejectFriendRequest(Long userId, Long requestId) {
        Friend request = friendRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
        
        // Verify this user is the recipient of the request
        if (!request.getFriendId().equals(userId)) {
            throw new IllegalArgumentException("Cannot reject this friend request");
        }
        
        // Check if the request is pending
        if (request.getStatus() != Friend.FriendStatus.PENDING) {
            throw new IllegalStateException("Friend request is not pending");
        }
        
        // Delete the request
        friendRepository.delete(request);
    }
    
    public void removeFriend(Long userId, Long friendId) {
        // Find friendship
        Optional<Friend> friendship = friendRepository.findFriendship(userId, friendId);
        
        if (friendship.isPresent() && friendship.get().getStatus() == Friend.FriendStatus.ACCEPTED) {
            friendRepository.delete(friendship.get());
        } else {
            throw new IllegalArgumentException("Friendship does not exist");
        }
    }
    
    public void blockUser(Long userId, Long userToBlockId) {
        // Check if there's an existing relationship
        Optional<Friend> existingRelationship = friendRepository.findFriendship(userId, userToBlockId);
        
        if (existingRelationship.isPresent()) {
            // Update existing relationship to blocked
            Friend relationship = existingRelationship.get();
            
            // Make sure the user doing the blocking is the 'userId'
            if (!relationship.getUserId().equals(userId)) {
                // Swap the users and create new entry
                friendRepository.delete(relationship);
                
                Friend blocked = new Friend();
                blocked.setUserId(userId);
                blocked.setFriendId(userToBlockId);
                blocked.setStatus(Friend.FriendStatus.BLOCKED);
                blocked.setCreatedAt(LocalDateTime.now());
                blocked.setLastUpdated(LocalDateTime.now());
                
                friendRepository.save(blocked);
            } else {
                // Update existing entry
                relationship.setStatus(Friend.FriendStatus.BLOCKED);
                relationship.setLastUpdated(LocalDateTime.now());
                friendRepository.save(relationship);
            }
        } else {
            // Create new blocked relationship
            Friend blocked = new Friend();
            blocked.setUserId(userId);
            blocked.setFriendId(userToBlockId);
            blocked.setStatus(Friend.FriendStatus.BLOCKED);
            blocked.setCreatedAt(LocalDateTime.now());
            blocked.setLastUpdated(LocalDateTime.now());
            
            friendRepository.save(blocked);
        }
    }
    
    public void unblockUser(Long userId, Long blockedUserId) {
        // Find the block relationship
        List<Friend> relationships = friendRepository.findByUserId(userId);
        
        for (Friend relationship : relationships) {
            if (relationship.getFriendId().equals(blockedUserId) && 
                relationship.getStatus() == Friend.FriendStatus.BLOCKED) {
                // Remove the block
                friendRepository.delete(relationship);
                return;
            }
        }
        
        throw new IllegalArgumentException("User is not blocked");
    }
    
    public List<Map<String, Object>> getBlockedUsers(Long userId) {
        // Get all blocked relationships
        List<Friend> blocked = friendRepository.findByUserIdAndStatus(userId, Friend.FriendStatus.BLOCKED);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friend block : blocked) {
            User blockedUser = userRepository.findById(block.getFriendId()).orElse(null);
            if (blockedUser == null) continue;
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", blockedUser.getId());
            userData.put("username", blockedUser.getUsername());
            
            result.add(userData);
        }
        
        return result;
    }
    
    public int getFriendCount(Long userId) {
        return friendRepository.countAcceptedFriendsByUserId(userId);
    }
}