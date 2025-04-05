package com.example.demo.service.gamification;

import com.example.demo.model.PlatformAchievement;
import com.example.demo.model.User;
import com.example.demo.repository.PlatformAchievementRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.realtime.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlatformAchievementService {
    
    private final PlatformAchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    // Define platform achievements
    private final List<Map<String, Object>> PLATFORM_ACHIEVEMENTS = Arrays.asList(
        Map.of(
            "id", "first_purchase",
            "name", "First Purchase",
            "description", "Buy your first game on the platform",
            "icon", "shopping_cart",
            "points", 10
        ),
        Map.of(
            "id", "collection_starter",
            "name", "Collection Starter",
            "description", "Own 5 games in your library",
            "icon", "library_books",
            "points", 20
        ),
        Map.of(
            "id", "game_enthusiast",
            "name", "Game Enthusiast",
            "description", "Own 25 games in your library",
            "icon", "stars",
            "points", 50
        ),
        Map.of(
            "id", "social_butterfly",
            "name", "Social Butterfly",
            "description", "Add 10 friends to your friends list",
            "icon", "people",
            "points", 30
        ),
        Map.of(
            "id", "review_contributor",
            "name", "Review Contributor",
            "description", "Write reviews for 5 different games",
            "icon", "rate_review",
            "points", 40
        ),
        Map.of(
            "id", "marathon_gamer",
            "name", "Marathon Gamer",
            "description", "Play games for a total of 100 hours",
            "icon", "timer",
            "points", 75
        ),
        Map.of(
            "id", "genre_explorer",
            "name", "Genre Explorer",
            "description", "Play games from 8 different genres",
            "icon", "explore",
            "points", 60
        ),
        Map.of(
            "id", "wishlist_planner",
            "name", "Wishlist Planner",
            "description", "Add 15 games to your wishlist",
            "icon", "favorite",
            "points", 25
        ),
        Map.of(
            "id", "profile_customizer",
            "name", "Profile Customizer",
            "description", "Customize your profile with a custom background and avatar",
            "icon", "brush",
            "points", 15
        ),
        Map.of(
            "id", "platform_veteran",
            "name", "Platform Veteran",
            "description", "Have an account for 1 year",
            "icon", "cake",
            "points", 100
        )
    );
    
    @Autowired
    public PlatformAchievementService(PlatformAchievementRepository achievementRepository,
                                     UserRepository userRepository,
                                     NotificationService notificationService) {
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    public List<Map<String, Object>> getAllPlatformAchievements() {
        return PLATFORM_ACHIEVEMENTS;
    }
    
    public List<Map<String, Object>> getUserAchievements(Long userId) {
        // Get all achievements this user has earned
        List<PlatformAchievement> userAchievements = achievementRepository.findByUserId(userId);
        
        // Create a set of achievement IDs the user has
        Set<String> earnedAchievementIds = userAchievements.stream()
            .map(PlatformAchievement::getAchievementId)
            .collect(Collectors.toSet());
        
        // Prepare response with all achievements and their status
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> achievement : PLATFORM_ACHIEVEMENTS) {
            String achievementId = (String) achievement.get("id");
            boolean isEarned = earnedAchievementIds.contains(achievementId);
            
            Map<String, Object> achievementData = new HashMap<>(achievement);
            achievementData.put("earned", isEarned);
            
            if (isEarned) {
                PlatformAchievement earned = userAchievements.stream()
                    .filter(a -> a.getAchievementId().equals(achievementId))
                    .findFirst()
                    .orElseThrow();
                
                achievementData.put("earnedDate", earned.getEarnedDate());
            }
            
            result.add(achievementData);
        }
        
        return result;
    }
    
    public void checkAndAwardAchievements(Long userId) {
        // Fetch user data
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get already earned achievements to avoid duplicates
        Set<String> earnedAchievementIds = achievementRepository.findByUserId(userId).stream()
            .map(PlatformAchievement::getAchievementId)
            .collect(Collectors.toSet());
        
        // Check each achievement condition
        // This would normally call other services to get the required data
        
        // Example: First Purchase
        if (!earnedAchievementIds.contains("first_purchase")) {
            boolean hasPurchased = true; // This would be checked via a purchase service
            if (hasPurchased) {
                awardAchievement(userId, "first_purchase");
            }
        }
        
        // Example: Collection Starter
        if (!earnedAchievementIds.contains("collection_starter")) {
            int gameCount = 10; // This would be checked via library service
            if (gameCount >= 5) {
                awardAchievement(userId, "collection_starter");
            }
        }
        
        // Example: Platform Veteran
        if (!earnedAchievementIds.contains("platform_veteran")) {
            LocalDateTime accountCreated = user.getCreatedAt();
            LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
            
            if (accountCreated.isBefore(oneYearAgo)) {
                awardAchievement(userId, "platform_veteran");
            }
        }
        
        // Additional achievement checks would be implemented similarly
    }
    
    public void awardAchievement(Long userId, String achievementId) {
        // Verify achievement exists
        boolean isValidAchievement = PLATFORM_ACHIEVEMENTS.stream()
            .anyMatch(a -> a.get("id").equals(achievementId));
        
        if (!isValidAchievement) {
            throw new IllegalArgumentException("Invalid achievement ID");
        }
        
        // Check if already earned
        if (achievementRepository.existsByUserIdAndAchievementId(userId, achievementId)) {
            return; // Already earned, do nothing
        }
        
        // Create and save achievement
        PlatformAchievement achievement = new PlatformAchievement();
        achievement.setUserId(userId);
        achievement.setAchievementId(achievementId);
        achievement.setEarnedDate(LocalDateTime.now());
        
        achievementRepository.save(achievement);
        
        // Get achievement details
        Map<String, Object> achievementDetails = PLATFORM_ACHIEVEMENTS.stream()
            .filter(a -> a.get("id").equals(achievementId))
            .findFirst()
            .orElseThrow();
        
        // Get points value
        int points = (int) achievementDetails.get("points");
        
        // Update user's achievement points
        User user = userRepository.findById(userId).orElseThrow();
        user.setAchievementPoints(user.getAchievementPoints() + points);
        userRepository.save(user);
        
        // Send notification
        notificationService.sendNotification(
            userId,
            "Achievement Unlocked",
            "You earned the " + achievementDetails.get("name") + " achievement!",
            "platform_achievement_unlocked",
            Map.of(
                "achievementId", achievementId,
                "points", points
            )
        );
    }
    
    public int getUserAchievementPoints(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getAchievementPoints();
    }
    
    public Map<String, Object> getAchievementDetails(String achievementId) {
        return PLATFORM_ACHIEVEMENTS.stream()
            .filter(a -> a.get("id").equals(achievementId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Achievement not found"));
    }
    
    public float getAchievementCompletion(Long userId) {
        int totalAchievements = PLATFORM_ACHIEVEMENTS.size();
        int earnedAchievements = achievementRepository.countAchievementsByUserId(userId);
        
        return (float) earnedAchievements / totalAchievements * 100;
    }
}