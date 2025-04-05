package com.example.demo.service.analytics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.model.Achievement;
import com.example.demo.model.Game;
import com.example.demo.model.PlatformAchievement;
import com.example.demo.repository.AchievementRepository;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.PlatformAchievementRepository;

@Service
public class AchievementTimelineService {
    
    private final AchievementRepository achievementRepository;
    private final PlatformAchievementRepository platformAchievementRepository;
    private final GameRepository gameRepository;
    
  
    public AchievementTimelineService(
            AchievementRepository achievementRepository,
            PlatformAchievementRepository platformAchievementRepository,
            GameRepository gameRepository) {
        this.achievementRepository = achievementRepository;
        this.platformAchievementRepository = platformAchievementRepository;
        this.gameRepository = gameRepository;
    }
    
    public Map<String, Object> getAchievementTimeline(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> timeline = new HashMap<>();
        
        // Convert to LocalDateTime for repository query
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        
        // Get game achievements
        List<Achievement> gameAchievements = 
            achievementRepository.findByUserIdAndDateBetween(userId, startDateTime, endDateTime);
        
        // Get platform achievements
        List<PlatformAchievement> platformAchievements = 
            platformAchievementRepository.findByUserIdAndEarnedDateBetween(userId, startDateTime, endDateTime);
        
        if (gameAchievements.isEmpty() && platformAchievements.isEmpty()) {
            timeline.put("totalAchievements", 0);
            timeline.put("timelineData", Collections.emptyList());
            return timeline;
        }
        
        // Get games info for achievements
        Set<Long> gameIds = gameAchievements.stream()
            .map(Achievement::getGameId)
            .collect(Collectors.toSet());
        
        List<Game> games = gameRepository.findAllById(gameIds);
        Map<Long, Game> gameMap = games.stream()
            .collect(Collectors.toMap(Game::getId, g -> g));
        
        // Calculate days between start and end
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        // Create day-by-day timeline
        List<Map<String, Object>> timelineData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Initialize daily counts map
        Map<String, List<Map<String, Object>>> achievementsByDay = new HashMap<>();
        
        // Process game achievements
        for (Achievement achievement : gameAchievements) {
            String dayKey = achievement.getDate().toLocalDate().format(formatter);
            
            if (!achievementsByDay.containsKey(dayKey)) {
                achievementsByDay.put(dayKey, new ArrayList<>());
            }
            
            Map<String, Object> achievementData = new HashMap<>();
            achievementData.put("id", achievement.getId());
            achievementData.put("name", achievement.getName());
            achievementData.put("description", achievement.getDescription());
            achievementData.put("timestamp", achievement.getDate().toString());
            achievementData.put("type", "game");
            achievementData.put("points", achievement.getPoints());
            
            // Add game info if available
            Game game = gameMap.get(achievement.getGameId());
            if (game != null) {
                achievementData.put("gameId", game.getId());
                achievementData.put("gameTitle", game.getTitle());
                achievementData.put("gameCoverUrl", game.getCoverImageUrl());
            }
            
            achievementsByDay.get(dayKey).add(achievementData);
        }
        
        // Process platform achievements
        for (PlatformAchievement achievement : platformAchievements) {
            String dayKey = achievement.getEarnedDate().toLocalDate().format(formatter);
            
            if (!achievementsByDay.containsKey(dayKey)) {
                achievementsByDay.put(dayKey, new ArrayList<>());
            }
            
            Map<String, Object> achievementData = new HashMap<>();
            achievementData.put("id", achievement.getId());
            achievementData.put("achievementId", achievement.getAchievementId());
            achievementData.put("timestamp", achievement.getEarnedDate().toString());
            achievementData.put("type", "platform");
            
            achievementsByDay.get(dayKey).add(achievementData);
        }
        
        // Create day entries for the entire range
        for (long day = 0; day < daysBetween; day++) {
            LocalDate currentDate = startDate.plusDays(day);
            String dayKey = currentDate.format(formatter);
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayKey);
            
            List<Map<String, Object>> dayAchievements = 
                achievementsByDay.getOrDefault(dayKey, Collections.emptyList());
            
            dayData.put("count", dayAchievements.size());
            dayData.put("achievements", dayAchievements);
            
            timelineData.add(dayData);
        }
        
        // Calculate total and add to response
        int totalAchievements = gameAchievements.size() + platformAchievements.size();
        timeline.put("totalAchievements", totalAchievements);
        timeline.put("startDate", startDate.toString());
        timeline.put("endDate", endDate.toString());
        timeline.put("timelineData", timelineData);
        
        // Calculate achievement streak
        int currentStreak = calculateCurrentStreak(timelineData);
        timeline.put("currentStreak", currentStreak);
        
        return timeline;
    }
    
    private int calculateCurrentStreak(List<Map<String, Object>> timelineData) {
        // Start from most recent day and count backwards
        int streak = 0;
        
        // Reverse the list to start from most recent
        for (int i = timelineData.size() - 1; i >= 0; i--) {
            Map<String, Object> day = timelineData.get(i);
            int count = (int) day.get("count");
            
            if (count > 0) {
                streak++;
            } else {
                // Break on first day with no achievements
                break;
            }
        }
        
        return streak;
    }
    
    public Map<String, Object> getAchievementSummary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // Get all user's achievements
        List<Achievement> gameAchievements = achievementRepository.findByUserId(userId);
        List<PlatformAchievement> platformAchievements = platformAchievementRepository.findByUserId(userId);
        
        int totalGameAchievements = gameAchievements.size();
        int totalPlatformAchievements = platformAchievements.size();
        
        summary.put("totalAchievements", totalGameAchievements + totalPlatformAchievements);
        summary.put("gameAchievements", totalGameAchievements);
        summary.put("platformAchievements", totalPlatformAchievements);
        
        // Get achievement counts by game
        Map<Long, Long> achievementsByGame = gameAchievements.stream()
            .collect(Collectors.groupingBy(Achievement::getGameId, Collectors.counting()));
        
        // Get games with most achievements
        List<Map.Entry<Long, Long>> topGames = achievementsByGame.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());
        
        // Get game details for top games
        List<Map<String, Object>> topGamesData = new ArrayList<>();
        if (!topGames.isEmpty()) {
            List<Long> topGameIds = topGames.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            Map<Long, Game> gameMap = gameRepository.findAllById(topGameIds).stream()
                .collect(Collectors.toMap(Game::getId, g -> g));
            
            for (Map.Entry<Long, Long> entry : topGames) {
                Game game = gameMap.get(entry.getKey());
                if (game == null) continue;
                
                Map<String, Object> gameData = new HashMap<>();
                gameData.put("id", game.getId());
                gameData.put("title", game.getTitle());
                gameData.put("coverUrl", game.getCoverImageUrl());
                gameData.put("achievementCount", entry.getValue());
                
                topGamesData.add(gameData);
            }
        }
        
        summary.put("topGames", topGamesData);
        
        // Get recent achievements (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Achievement> recentGameAchievements = 
            achievementRepository.findByUserIdAndDateAfter(userId, thirtyDaysAgo);
        List<PlatformAchievement> recentPlatformAchievements = 
            platformAchievementRepository.findByUserIdAndEarnedDateAfter(userId, thirtyDaysAgo);
        
        summary.put("recentAchievements", recentGameAchievements.size() + recentPlatformAchievements.size());
        
        // Calculate total achievement points
        int totalPoints = gameAchievements.stream()
            .mapToInt(Achievement::getPoints)
            .sum();
        
        summary.put("totalPoints", totalPoints);
        
        return summary;
    }
    
    public List<Map<String, Object>> getRecentAchievements(Long userId, int limit) {
        // Get recent achievements (last 90 days)
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        List<Achievement> recentAchievements = 
            achievementRepository.findByUserIdAndDateAfter(userId, ninetyDaysAgo);
        
        // Sort by date (most recent first)
        recentAchievements.sort(Comparator.comparing(Achievement::getDate).reversed());
        
        // Limit to requested amount
        recentAchievements = recentAchievements.stream()
            .limit(limit)
            .collect(Collectors.toList());
        
        // Get game details
        Set<Long> gameIds = recentAchievements.stream()
            .map(Achievement::getGameId)
            .collect(Collectors.toSet());
        
        Map<Long, Game> gameMap = gameRepository.findAllById(gameIds).stream()
            .collect(Collectors.toMap(Game::getId, g -> g));
        
        // Format achievements
        List<Map<String, Object>> result = new ArrayList<>();
        for (Achievement achievement : recentAchievements) {
            Map<String, Object> achievementData = new HashMap<>();
            achievementData.put("id", achievement.getId());
            achievementData.put("name", achievement.getName());
            achievementData.put("description", achievement.getDescription());
            achievementData.put("iconUrl", achievement.getIconUrl());
            achievementData.put("date", achievement.getDate());
            achievementData.put("points", achievement.getPoints());
            
            Game game = gameMap.get(achievement.getGameId());
            if (game != null) {
                achievementData.put("gameId", game.getId());
                achievementData.put("gameTitle", game.getTitle());
                achievementData.put("gameCoverUrl", game.getCoverImageUrl());
            }
            
            result.add(achievementData);
        }
        
        return result;
    }
}