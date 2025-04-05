package com.example.demo.service.realtime;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class LiveUpdateService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public LiveUpdateService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    public void sendUserUpdate(Long userId, String updateType, Object data) {
        String destination = "/topic/user/" + userId + "/updates";
        Map<String, Object> payload = Map.of(
            "type", updateType,
            "timestamp", System.currentTimeMillis(),
            "data", data
        );
        
        messagingTemplate.convertAndSend(destination, payload);
    }
    
    public void sendFriendStatusUpdate(Long userId, Long friendId, String status) {
        sendUserUpdate(userId, "friend_status", Map.of(
            "friendId", friendId,
            "status", status
        ));
    }
    
    public void sendGamePriceUpdate(Long userId, Long gameId, double newPrice, boolean onSale) {
        sendUserUpdate(userId, "game_price", Map.of(
            "gameId", gameId,
            "price", newPrice,
            "onSale", onSale
        ));
    }
    
    public void sendAchievementEarned(Long userId, String achievementId) {
        sendUserUpdate(userId, "achievement_earned", Map.of(
            "achievementId", achievementId
        ));
    }
    
    public void sendLibraryUpdate(Long userId, Long gameId, String updateType) {
        sendUserUpdate(userId, "library_update", Map.of(
            "gameId", gameId,
            "updateType", updateType
        ));
    }
    
    public void broadcastStoreUpdate(Map<String, Object> storeData) {
        messagingTemplate.convertAndSend("/topic/store/updates", Map.of(
            "type", "store_update",
            "timestamp", System.currentTimeMillis(),
            "data", storeData
        ));
    }
    
    public void sendAmbientUpdate(Long userId, Map<String, Object> ambientData) {
        sendUserUpdate(userId, "ambient_update", ambientData);
    }
    
    public void sendChatMessage(Long userId, Long senderId, String senderName, String message) {
        sendUserUpdate(userId, "chat_message", Map.of(
            "senderId", senderId,
            "senderName", senderName,
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    public void sendTypingIndicator(Long userId, Long friendId, boolean isTyping) {
        sendUserUpdate(userId, "typing_indicator", Map.of(
            "friendId", friendId,
            "isTyping", isTyping
        ));
    }
    
    public void sendGameInvite(Long userId, Long fromUserId, String fromUsername, Long gameId, String gameTitle) {
        sendUserUpdate(userId, "game_invite", Map.of(
            "fromUserId", fromUserId,
            "fromUsername", fromUsername,
            "gameId", gameId,
            "gameTitle", gameTitle
        ));
    }
    
    public void sendDealAlert(Long userId, Long gameId, String gameTitle, double originalPrice, double salePrice) {
        sendUserUpdate(userId, "deal_alert", Map.of(
            "gameId", gameId,
            "gameTitle", gameTitle,
            "originalPrice", originalPrice,
            "salePrice", salePrice,
            "discount", Math.round((1 - (salePrice / originalPrice)) * 100)
        ));
    }
}