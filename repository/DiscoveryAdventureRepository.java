package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.DiscoveryAdventure;

@Repository
public interface DiscoveryAdventureRepository extends JpaRepository<DiscoveryAdventure, Long> {
    
    List<DiscoveryAdventure> findByUserId(Long userId);
    
    List<DiscoveryAdventure> findByUserIdAndCompleted(Long userId, boolean completed);
    
    Optional<DiscoveryAdventure> findByUserIdAndAdventureId(Long userId, String adventureId);
    
    boolean existsByUserIdAndAdventureId(Long userId, String adventureId);
    
    @Query("SELECT COUNT(d) FROM DiscoveryAdventure d WHERE d.userId = :userId AND d.completed = true")
    int countCompletedAdventuresByUserId(@Param("userId") Long userId);
}