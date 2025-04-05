package com.example.demo.service.realtime;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    public void sendToUser(String destination, Object payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }
    
    public void sendToTopic(String topic, Object payload) {
        messagingTemplate.convertAndSend("/topic/" + topic, payload);
    }
    
    public void sendToUserQueue(String username, Object payload) {
        messagingTemplate.convertAndSendToUser(username, "/queue/messages", payload);
    }
    
    public void broadcastToAll(String message) {
        Map<String, Object> payload = Map.of(
            "type", "broadcast",
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/broadcast", payload);
    }
    
    public void notifyUser(String username, String title, String message) {
        Map<String, Object> payload = Map.of(
            "type", "notification",
            "title", title,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload);
    }
    
    public void sendFriendRequest(String username, Long fromUserId, String fromUsername) {
        Map<String, Object> payload = Map.of(
            "type", "friend_request",
            "fromUserId", fromUserId,
            "fromUsername", fromUsername,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSendToUser(username, "/queue/friend-requests", payload);
    }
    
    public void sendGameInvite(String username, Long fromUserId, String fromUsername, Long gameId, String gameTitle) {
        Map<String, Object> payload = Map.of(
            "type", "game_invite",
            "fromUserId", fromUserId,
            "fromUsername", fromUsername,
            "gameId", gameId,
            "gameTitle", gameTitle,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSendToUser(username, "/queue/game-invites", payload);
    }
    
    public void broadcastOnlineStatus(String username, boolean online) {
        Map<String, Object> payload = Map.of(
            "type", "status_update",
            "username", username,
            "online", online,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/user-status", payload);
    }
    
    public void broadcastGameStatus(String username, Long gameId, String gameTitle, boolean playing) {
        Map<String, Object> payload = Map.of(
            "type", "game_status",
            "username", username,
            "gameId", gameId,
            "gameTitle", gameTitle,
            "playing", playing,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/game-status", payload);
    }
}