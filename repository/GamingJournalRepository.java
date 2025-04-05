package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.GamingJournal;

@Repository
public interface GamingJournalRepository extends JpaRepository<GamingJournal, Long> {
    
    List<GamingJournal> findByUserId(Long userId);
    
    List<GamingJournal> findByUserIdOrderByGeneratedDateDesc(Long userId);
    
    Optional<GamingJournal> findFirstByUserIdAndPeriodOrderByGeneratedDateDesc(Long userId, String period);
    
    @Query("SELECT g FROM GamingJournal g WHERE g.userId = :userId AND g.generatedDate >= :date ORDER BY g.generatedDate DESC")
    List<GamingJournal> findRecentByUserIdAfterDate(
            @Param("userId") Long userId,
            @Param("date") LocalDateTime date);
}