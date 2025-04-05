package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<Notification> findByUserIdAndReadFalseOrderByTimestampDesc(Long userId);
    
    List<Notification> findByUserIdAndTypeOrderByTimestampDesc(Long userId, String type);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.timestamp >= :date ORDER BY n.timestamp DESC")
    List<Notification> findByUserIdAndAfterDate(
            @Param("userId") Long userId,
            @Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.read = false")
    int countByUserIdAndReadFalse(@Param("userId") Long userId);
}