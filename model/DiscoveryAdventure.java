package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discovery_adventures",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "adventure_id"}))
public class DiscoveryAdventure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "adventure_id", nullable = false)
    private String adventureId;
    
    private LocalDateTime startDate;
    
    private LocalDateTime completedDate;
    
    private boolean completed;
    
    private int currentStep;
    
    @Column(name = "current_game_id")
    private Long currentGameId;
    
    @ElementCollection
    @CollectionTable(name = "discovery_adventure_games", 
                    joinColumns = @JoinColumn(name = "adventure_id"))
    @Column(name = "game_id")
    private List<Long> completedGameIds = new ArrayList<>();
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAdventureId() {
        return adventureId;
    }

    public void setAdventureId(String adventureId) {
        this.adventureId = adventureId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public Long getCurrentGameId() {
        return currentGameId;
    }

    public void setCurrentGameId(Long currentGameId) {
        this.currentGameId = currentGameId;
    }

    public List<Long> getCompletedGameIds() {
        return completedGameIds;
    }

    public void setCompletedGameIds(List<Long> completedGameIds) {
        this.completedGameIds = completedGameIds;
    }
}