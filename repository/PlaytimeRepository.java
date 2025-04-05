package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Playtime;

@Repository
public interface PlaytimeRepository extends JpaRepository<Playtime, Long> {
    
    List<Playtime> findByUserId(Long userId);
    
    List<Playtime> findByUserIdAndGameId(Long userId, Long gameId);
    
    @Query("SELECT p FROM Playtime p WHERE p.userId = :userId AND p.sessionStart >= :startDate")
    List<Playtime> findByUserIdAndAfterDate(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT SUM(p.duration) FROM Playtime p WHERE p.userId = :userId")
    Integer getTotalPlaytimeByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(p.duration) FROM Playtime p WHERE p.userId = :userId AND p.gameId = :gameId")
    Integer getTotalPlaytimeByUserIdAndGameId(@Param("userId") Long userId, 
                                             @Param("gameId") Long gameId);
    
    @Query("SELECT p.gameId, SUM(p.duration) as totalTime FROM Playtime p WHERE p.userId = :userId " +
           "GROUP BY p.gameId ORDER BY totalTime DESC")
    List<Object[]> getMostPlayedGamesByUserId(@Param("userId") Long userId);
}