package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.PlatformAchievement;

@Repository
public interface PlatformAchievementRepository extends JpaRepository<PlatformAchievement, Long> {
    
    List<PlatformAchievement> findByUserId(Long userId);
    
    Optional<PlatformAchievement> findByUserIdAndAchievementId(Long userId, String achievementId);
    
    boolean existsByUserIdAndAchievementId(Long userId, String achievementId);
    
    @Query("SELECT p FROM PlatformAchievement p WHERE p.userId = :userId AND p.earnedDate >= :date")
    List<PlatformAchievement> findByUserIdAndEarnedDateAfter(
            @Param("userId") Long userId, 
            @Param("date") LocalDateTime date);
    
    @Query("SELECT p FROM PlatformAchievement p WHERE p.userId = :userId AND p.earnedDate BETWEEN :startDate AND :endDate")
    List<PlatformAchievement> findByUserIdAndEarnedDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM PlatformAchievement p WHERE p.userId = :userId")
    int countAchievementsByUserId(@Param("userId") Long userId);
}