package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.CollectionQuest;

@Repository
public interface CollectionQuestRepository extends JpaRepository<CollectionQuest, Long> {
    
    List<CollectionQuest> findByUserId(Long userId);
    
    List<CollectionQuest> findByUserIdAndCompleted(Long userId, boolean completed);
    
    Optional<CollectionQuest> findByUserIdAndQuestId(Long userId, String questId);
    
    boolean existsByUserIdAndQuestId(Long userId, String questId);
    
    @Query("SELECT COUNT(c) FROM CollectionQuest c WHERE c.userId = :userId AND c.completed = true")
    int countCompletedQuestsByUserId(@Param("userId") Long userId);
}