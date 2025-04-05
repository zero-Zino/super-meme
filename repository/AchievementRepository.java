package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Achievement;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    
    List<Achievement> findByUserId(Long userId);
    
    List<Achievement> findByUserIdAndGameId(Long userId, Long gameId);
    
    Optional<Achievement> findByUserIdAndGameIdAndAchievementCode(Long userId, Long gameId, String achievementCode);
    
    boolean existsByUserIdAndGameIdAndAchievementCode(Long userId, Long gameId, String achievementCode);
    
    @Query("SELECT a FROM Achievement a WHERE a.userId = :userId AND a.date >= :date")
    List<Achievement> findByUserIdAndDateAfter(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    
    @Query("SELECT a FROM Achievement a WHERE a.userId = :userId AND a.date BETWEEN :startDate AND :endDate")
    List<Achievement> findByUserIdAndDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.userId = :userId AND a.gameId = :gameId")
    int countAchievementsByUserIdAndGameId(@Param("userId") Long userId, @Param("gameId") Long gameId);
    
    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.userId = :userId")
    int countAchievementsByUserId(@Param("userId") Long userId);
}