package com.example.demo.service.realtime;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final LiveUpdateService liveUpdateService;
    
    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            LiveUpdateService liveUpdateService) {
        this.notificationRepository = notificationRepository;
        this.liveUpdateService = liveUpdateService;
    }
    
    public Notification sendNotification(Long userId, String title, String message, 
                                       String type, Map<String, Object> data) {
        // Create notification
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setData(data.toString()); // Convert to string for storage
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        
        // Save to database
        Notification saved = notificationRepository.save(notification);
        
        // Send real-time update
        liveUpdateService.sendUserUpdate(userId, "notification", Map.of(
            "id", saved.getId(),
            "title", title,
            "message", message,
            "type", type,
            "data", data,
            "timestamp", saved.getTimestamp().toString(),
            "read", false
        ));
        
        return saved;
    }
    
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByTimestampDesc(userId);
    }
    
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByTimestampDesc(userId);
        
        for (Notification notification : unread) {
            notification.setRead(true);
        }
        
        notificationRepository.saveAll(unread);
    }
    
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
}