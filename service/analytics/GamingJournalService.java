package com.example.demo.service.analytics;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.model.Achievement;
import com.example.demo.model.Game;
import com.example.demo.model.GamingJournal;
import com.example.demo.model.Playtime;
import com.example.demo.repository.AchievementRepository;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.GamingJournalRepository;
import com.example.demo.repository.PlaytimeRepository;

@Service
public class GamingJournalService {
    
    private final GamingJournalRepository journalRepository;
    private final PlaytimeRepository playtimeRepository;
    private final AchievementRepository achievementRepository;
    private final GameRepository gameRepository;
    

    public GamingJournalService(
            GamingJournalRepository journalRepository,
            PlaytimeRepository playtimeRepository,
            AchievementRepository achievementRepository,
            GameRepository gameRepository) {
        this.journalRepository = journalRepository;
        this.playtimeRepository = playtimeRepository;
        this.achievementRepository = achievementRepository;
        this.gameRepository = gameRepository;
    }
    
    public Map<String, Object> generateGamingJournal(Long userId, String period) {
        Map<String, Object> journal = new HashMap<>();
        
        // Determine start and end dates based on period
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();
        
        switch (period.toLowerCase()) {
            case "weekly":
                startDate = LocalDateTime.now().with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
                break;
            case "monthly":
                startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
                break;
            case "yearly":
                startDate = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0);
                break;
            default:
                startDate = LocalDateTime.now().minusDays(7);
        }
        
        // Get playtime data for the period
        List<Playtime> playtimes = playtimeRepository.findByUserIdAndAfterDate(userId, startDate);
        
        // Calculate total playtime
        int totalMinutes = playtimes.stream().mapToInt(Playtime::getDuration).sum();
        
        // Get unique games played
        Set<Long> uniqueGameIds = playtimes.stream()
            .map(Playtime::getGameId)
            .collect(Collectors.toSet());
        
        // Get achievements earned during this period
        List<Achievement> achievements = achievementRepository.findByUserIdAndDateAfter(userId, startDate);
        
        // Get game details
        List<Game> games = gameRepository.findAllById(uniqueGameIds);
        Map<Long, Game> gameMap = games.stream()
            .collect(Collectors.toMap(Game::getId, g -> g));
        
        // Calculate most played game
        Map<Long, Integer> gamePlaytimes = new HashMap<>();
        for (Playtime pt : playtimes) {
            gamePlaytimes.put(pt.getGameId(), 
                              gamePlaytimes.getOrDefault(pt.getGameId(), 0) + pt.getDuration());
        }
        
        // Find the game with max playtime
        Map.Entry<Long, Integer> mostPlayed = gamePlaytimes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        // Build journal data
        journal.put("period", period);
        journal.put("startDate", startDate.toString());
        journal.put("endDate", endDate.toString());
        journal.put("totalPlaytime", totalMinutes);
        journal.put("gamesPlayed", uniqueGameIds.size());
        journal.put("achievementsEarned", achievements.size());
        
        // Add most played game if available
        if (mostPlayed != null && gameMap.containsKey(mostPlayed.getKey())) {
            Game topGame = gameMap.get(mostPlayed.getKey());
            Map<String, Object> mostPlayedGame = new HashMap<>();
            mostPlayedGame.put("id", topGame.getId());
            mostPlayedGame.put("title", topGame.getTitle());
            mostPlayedGame.put("coverUrl", topGame.getCoverImageUrl());
            mostPlayedGame.put("playtime", mostPlayed.getValue());
            journal.put("mostPlayedGame", mostPlayedGame);
        }
        
        // Add game details
        List<Map<String, Object>> gameDetails = new ArrayList<>();
        for (Long gameId : uniqueGameIds) {
            if (!gameMap.containsKey(gameId)) continue;
            
            Game game = gameMap.get(gameId);
            int gamePlaytime = gamePlaytimes.getOrDefault(gameId, 0);
            
            // Count achievements for this game
            long gameAchievements = achievements.stream()
                .filter(a -> a.getGameId().equals(gameId))
                .count();
            
            Map<String, Object> gameDetail = new HashMap<>();
            gameDetail.put("id", game.getId());
            gameDetail.put("title", game.getTitle());
            gameDetail.put("coverUrl", game.getCoverImageUrl());
            gameDetail.put("playtime", gamePlaytime);
            gameDetail.put("achievements", gameAchievements);
            
            gameDetails.add(gameDetail);
        }
        
        // Sort by playtime (descending)
        gameDetails.sort((g1, g2) -> {
            Integer p1 = (Integer) g1.get("playtime");
            Integer p2 = (Integer) g2.get("playtime");
            return p2.compareTo(p1);
        });
        
        journal.put("games", gameDetails);
        
        // Generate insights based on the data
        List<String> insights = generateInsights(
            totalMinutes, uniqueGameIds.size(), achievements.size(), 
            mostPlayed != null ? mostPlayed.getValue() : 0);
        
        journal.put("insights", insights);
        
        // Save journal entry
        saveJournalEntry(userId, period, journal);
        
        return journal;
    }
    
    private List<String> generateInsights(int totalMinutes, int gamesCount, 
                                         int achievementsCount, int topGameMinutes) {
        List<String> insights = new ArrayList<>();
        
        // Generate different insights based on the data
        if (totalMinutes > 600) { // More than 10 hours
            insights.add("You've had an intense gaming period with over 10 hours of gameplay!");
        } else if (totalMinutes < 60) { // Less than 1 hour
            insights.add("Looks like a light gaming period with less than an hour of play.");
        }
        
        if (gamesCount > 3) {
            insights.add("You've explored a variety of games during this period.");
        } else if (gamesCount == 1) {
            insights.add("You've focused exclusively on one game this period.");
        }
        
        if (achievementsCount > 5) {
            insights.add("Great achievement hunting! You've earned " + achievementsCount + " achievements.");
        }
        
        if (topGameMinutes > 300) { // More than 5 hours on a single game
            insights.add("You've really invested in your top game with over 5 hours of playtime!");
        }
        
        return insights;
    }
    
    private void saveJournalEntry(Long userId, String period, Map<String, Object> journalData) {
        GamingJournal journal = new GamingJournal();
        journal.setUserId(userId);
        journal.setPeriod(period);
        journal.setGeneratedDate(LocalDateTime.now());
        journal.setJournalData(journalData.toString()); // Convert to string for storage
        
        journalRepository.save(journal);
    }
    
    public Map<String, Object> getRecentHighlights(Long userId) {
        Map<String, Object> highlights = new HashMap<>();
        
        // Get recent play sessions (last 7 days)
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<Playtime> recentSessions = playtimeRepository.findByUserIdAndAfterDate(userId, lastWeek);
        
        if (recentSessions.isEmpty()) {
            highlights.put("hasActivity", false);
            return highlights;
        }
        
        highlights.put("hasActivity", true);
        
        // Calculate total recent playtime
        int recentPlaytime = recentSessions.stream().mapToInt(Playtime::getDuration).sum();
        highlights.put("recentPlaytime", recentPlaytime);
        
        // Get recent achievements
        List<Achievement> recentAchievements = 
            achievementRepository.findByUserIdAndDateAfter(userId, lastWeek);
        highlights.put("recentAchievements", recentAchievements.size());
        
        // Get most active day
        Map<LocalDate, Integer> dailyPlaytime = new HashMap<>();
        for (Playtime session : recentSessions) {
            LocalDate date = session.getSessionStart().toLocalDate();
            dailyPlaytime.put(date, 
                             dailyPlaytime.getOrDefault(date, 0) + session.getDuration());
        }
        
        Map.Entry<LocalDate, Integer> mostActiveDay = dailyPlaytime.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        if (mostActiveDay != null) {
            highlights.put("mostActiveDay", mostActiveDay.getKey().toString());
            highlights.put("mostActiveDayPlaytime", mostActiveDay.getValue());
        }
        
        // Get recently played games
        Set<Long> recentGameIds = recentSessions.stream()
            .map(Playtime::getGameId)
            .collect(Collectors.toSet());
        
        List<Game> recentGames = gameRepository.findAllById(recentGameIds);
        
        // Calculate playtime per game
        Map<Long, Integer> gamePlaytimes = new HashMap<>();
        for (Playtime session : recentSessions) {
            gamePlaytimes.put(session.getGameId(), 
                             gamePlaytimes.getOrDefault(session.getGameId(), 0) + session.getDuration());
        }
        
        // Create recent games list with playtimes
        List<Map<String, Object>> recentGamesList = new ArrayList<>();
        for (Game game : recentGames) {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id", game.getId());
            gameData.put("title", game.getTitle());
            gameData.put("coverUrl", game.getCoverImageUrl());
            gameData.put("playtime", gamePlaytimes.getOrDefault(game.getId(), 0));
            
            recentGamesList.add(gameData);
        }
        
        // Sort by playtime (descending)
        recentGamesList.sort((g1, g2) -> {
            Integer p1 = (Integer) g1.get("playtime");
            Integer p2 = (Integer) g2.get("playtime");
            return p2.compareTo(p1);
        });
        
        highlights.put("recentGames", recentGamesList);
        
        // Get notable achievements
        if (!recentAchievements.isEmpty()) {
            List<Map<String, Object>> notableAchievements = new ArrayList<>();
            
            // Sort by points (descending)
            List<Achievement> sortedAchievements = recentAchievements.stream()
                .sorted(Comparator.comparing(Achievement::getPoints).reversed())
                .limit(3)
                .collect(Collectors.toList());
            
            // Get game details for these achievements
            Set<Long> gameIds = sortedAchievements.stream()
                .map(Achievement::getGameId)
                .collect(Collectors.toSet());
            
            Map<Long, Game> gameMap = gameRepository.findAllById(gameIds).stream()
                .collect(Collectors.toMap(Game::getId, g -> g));
            
            for (Achievement achievement : sortedAchievements) {
                Map<String, Object> achievementData = new HashMap<>();
                achievementData.put("id", achievement.getId());
                achievementData.put("name", achievement.getName());
                achievementData.put("description", achievement.getDescription());
                achievementData.put("iconUrl", achievement.getIconUrl());
                achievementData.put("points", achievement.getPoints());
                achievementData.put("date", achievement.getDate());
                
                Game game = gameMap.get(achievement.getGameId());
                if (game != null) {
                    achievementData.put("gameTitle", game.getTitle());
                }
                
                notableAchievements.add(achievementData);
            }
            
            highlights.put("notableAchievements", notableAchievements);
        }
        
        return highlights;
    }
    
    public List<Map<String, Object>> getUserJournals(Long userId) {
        List<GamingJournal> journals = journalRepository.findByUserIdOrderByGeneratedDateDesc(userId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (GamingJournal journal : journals) {
            Map<String, Object> journalData = new HashMap<>();
            journalData.put("id", journal.getId());
            journalData.put("period", journal.getPeriod());
            journalData.put("generatedDate", journal.getGeneratedDate());
            
            // Parse journal data
            // In a real implementation, you would parse the JSON string
            // For now, we'll just include the metadata
            
            result.add(journalData);
        }
        
        return result;
    }
    
    public Map<String, Object> getJournalById(Long journalId) {
        GamingJournal journal = journalRepository.findById(journalId)
            .orElseThrow(() -> new IllegalArgumentException("Journal not found"));
        
        // In a real implementation, you would parse the journalData
        // For now, we'll just return a simple representation
        Map<String, Object> result = new HashMap<>();
        result.put("id", journal.getId());
        result.put("period", journal.getPeriod());
        result.put("generatedDate", journal.getGeneratedDate());
        result.put("data", "Journal data would be parsed here");
        
        return result;
    }
}