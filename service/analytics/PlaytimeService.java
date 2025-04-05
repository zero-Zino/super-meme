package com.example.demo.service.analytics;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.model.Game;
import com.example.demo.model.Library;
import com.example.demo.model.Playtime;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.LibraryRepository;
import com.example.demo.repository.PlaytimeRepository;

@Service
public class PlaytimeService {
    
    private final PlaytimeRepository playtimeRepository;
    private final LibraryRepository libraryRepository;
    private final GameRepository gameRepository;

    public PlaytimeService(PlaytimeRepository playtimeRepository, 
                           LibraryRepository libraryRepository,
                           GameRepository gameRepository) {
        this.playtimeRepository = playtimeRepository;
        this.libraryRepository = libraryRepository;
        this.gameRepository = gameRepository;
    }
    
    public void recordGameSession(Long userId, Long gameId, int minutesPlayed) {
        // Update library entry
        Library library = libraryRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not in user's library"));
        
        library.setPlaytime(library.getPlaytime() + minutesPlayed);
        library.setLastPlayed(LocalDateTime.now());
        libraryRepository.save(library);
        
        // Record detailed playtime entry
        Playtime playtime = new Playtime();
        playtime.setUserId(userId);
        playtime.setGameId(gameId);
        playtime.setSessionStart(LocalDateTime.now().minus(minutesPlayed, ChronoUnit.MINUTES));
        playtime.setSessionEnd(LocalDateTime.now());
        playtime.setDuration(minutesPlayed);
        
        playtimeRepository.save(playtime);
    }
    
    public Map<String, Object> getPlaytimeStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get all playtime records for user
        List<Playtime> playtimes = playtimeRepository.findByUserId(userId);
        
        if (playtimes.isEmpty()) {
            stats.put("totalPlaytime", 0);
            stats.put("gamesPlayed", 0);
            stats.put("dailyAverage", 0);
            stats.put("weeklyData", Collections.emptyList());
            stats.put("gameBreakdown", Collections.emptyList());
            return stats;
        }
        
        // Calculate total playtime
        int totalMinutes = playtimes.stream()
            .mapToInt(Playtime::getDuration)
            .sum();
        
        // Games played count
        Set<Long> uniqueGames = playtimes.stream()
            .map(Playtime::getGameId)
            .collect(Collectors.toSet());
        
        // Get date of first play session
        LocalDateTime firstSession = playtimes.stream()
            .min(Comparator.comparing(Playtime::getSessionStart))
            .map(Playtime::getSessionStart)
            .orElse(LocalDateTime.now());
        
        long daysSinceFirst = ChronoUnit.DAYS.between(firstSession.toLocalDate(), LocalDate.now()) + 1;
        double dailyAverage = totalMinutes / (double) daysSinceFirst;
        
        stats.put("totalPlaytime", totalMinutes);
        stats.put("gamesPlayed", uniqueGames.size());
        stats.put("dailyAverage", Math.round(dailyAverage * 10) / 10.0); // Round to 1 decimal place
        
        // Weekly breakdown
        Map<LocalDate, Integer> dailyPlaytime = new HashMap<>();
        
        // Start with Sunday of last week
        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        
        // Initialize all days with zero
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            dailyPlaytime.put(date, 0);
        }
        
        // Fill in actual playtime data
        for (Playtime session : playtimes) {
            LocalDate sessionDate = session.getSessionStart().toLocalDate();
            if (!sessionDate.isBefore(startDate) && !sessionDate.isAfter(startDate.plusDays(6))) {
                dailyPlaytime.put(sessionDate, dailyPlaytime.getOrDefault(sessionDate, 0) + session.getDuration());
            }
        }
        
        // Convert to list for frontend
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> day = new HashMap<>();
            day.put("date", date.toString());
            day.put("dayName", date.getDayOfWeek().toString().substring(0, 3));
            day.put("minutes", dailyPlaytime.getOrDefault(date, 0));
            weeklyData.add(day);
        }
        
        stats.put("weeklyData", weeklyData);
        
        // Game breakdown
        Map<Long, Integer> gamePlaytimes = new HashMap<>();
        for (Playtime session : playtimes) {
            gamePlaytimes.put(session.getGameId(), 
                              gamePlaytimes.getOrDefault(session.getGameId(), 0) + session.getDuration());
        }
        
        // Get game details and sort by playtime
        List<Map<String, Object>> gameBreakdown = new ArrayList<>();
        List<Long> gameIds = new ArrayList<>(gamePlaytimes.keySet());
        Map<Long, Game> games = gameRepository.findAllById(gameIds).stream()
            .collect(Collectors.toMap(Game::getId, game -> game));
        
        gamePlaytimes.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .forEach(entry -> {
                Game game = games.get(entry.getKey());
                if (game == null) return;
                
                Map<String, Object> gameData = new HashMap<>();
                gameData.put("id", game.getId());
                gameData.put("title", game.getTitle());
                gameData.put("coverUrl", game.getCoverImageUrl());
                gameData.put("playtime", entry.getValue());
                gameData.put("percentage", Math.round((entry.getValue() / (double) totalMinutes) * 100));
                
                gameBreakdown.add(gameData);
            });
        
        stats.put("gameBreakdown", gameBreakdown);
        
        return stats;
    }
    
    public Map<String, Object> getPlaytimeTrends(Long userId, int days) {
        Map<String, Object> trends = new HashMap<>();
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Playtime> playtimes = playtimeRepository.findByUserIdAndAfterDate(userId, startDate);
        
        if (playtimes.isEmpty()) {
            trends.put("totalPlaytime", 0);
            trends.put("dailyData", Collections.emptyList());
            trends.put("timeOfDayData", Collections.emptyList());
            return trends;
        }
        
        // Calculate total playtime for period
        int totalMinutes = playtimes.stream()
            .mapToInt(Playtime::getDuration)
            .sum();
        
        trends.put("totalPlaytime", totalMinutes);
        
        // Daily breakdown
        Map<LocalDate, Integer> dailyPlaytime = new HashMap<>();
        
        // Initialize all days with zero
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            dailyPlaytime.put(date, 0);
        }
        
        // Fill in actual playtime data
        for (Playtime session : playtimes) {
            LocalDate sessionDate = session.getSessionStart().toLocalDate();
            dailyPlaytime.put(sessionDate, dailyPlaytime.getOrDefault(sessionDate, 0) + session.getDuration());
        }
        
        // Convert to list for frontend, sorted by date
        List<Map<String, Object>> dailyData = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> day = new HashMap<>();
            day.put("date", date.toString());
            day.put("minutes", dailyPlaytime.getOrDefault(date, 0));
            dailyData.add(day);
        }
        
        trends.put("dailyData", dailyData);
        
        // Time of day breakdown (by hour)
        Map<Integer, Integer> hourlyPlaytime = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyPlaytime.put(i, 0);
        }
        
        for (Playtime session : playtimes) {
            // This is a simplification; a real implementation would distribute the minutes across hours
            int hour = session.getSessionStart().getHour();
            hourlyPlaytime.put(hour, hourlyPlaytime.get(hour) + session.getDuration());
        }
        
        // Convert to list for frontend
        List<Map<String, Object>> timeOfDayData = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", i);
            hourData.put("minutes", hourlyPlaytime.get(i));
            timeOfDayData.add(hourData);
        }
        
        trends.put("timeOfDayData", timeOfDayData);
        
        // Game breakdown for period
        Map<Long, Integer> gamePlaytimes = new HashMap<>();
        for (Playtime session : playtimes) {
            gamePlaytimes.put(session.getGameId(), 
                             gamePlaytimes.getOrDefault(session.getGameId(), 0) + session.getDuration());
        }
        
        // Get game details and sort by playtime
        List<Map<String, Object>> gameBreakdown = new ArrayList<>();
        List<Long> gameIds = new ArrayList<>(gamePlaytimes.keySet());
        Map<Long, Game> games = gameRepository.findAllById(gameIds).stream()
            .collect(Collectors.toMap(Game::getId, game -> game));
        
        gamePlaytimes.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .limit(5) // Top 5 games
            .forEach(entry -> {
                Game game = games.get(entry.getKey());
                if (game == null) return;
                
                Map<String, Object> gameData = new HashMap<>();
                gameData.put("id", game.getId());
                gameData.put("title", game.getTitle());
                gameData.put("coverUrl", game.getCoverImageUrl());
                gameData.put("playtime", entry.getValue());
                gameData.put("percentage", Math.round((entry.getValue() / (double) totalMinutes) * 100));
                
                gameBreakdown.add(gameData);
            });
        
        trends.put("topGames", gameBreakdown);
        
        return trends;
    }
    
    public List<Map<String, Object>> getMostPlayedGames(Long userId, int limit) {
        List<Object[]> results = playtimeRepository.getMostPlayedGamesByUserId(userId);
        
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Get game IDs
        List<Long> gameIds = results.stream()
            .map(row -> (Long) row[0])
            .collect(Collectors.toList());
        
        // Get game details
        Map<Long, Game> games = gameRepository.findAllById(gameIds).stream()
            .collect(Collectors.toMap(Game::getId, game -> game));
        
        // Create result list
        List<Map<String, Object>> mostPlayed = new ArrayList<>();
        int count = 0;
        
        for (Object[] row : results) {
            if (count >= limit) break;
            
            Long gameId = (Long) row[0];
            Integer minutes = ((Number) row[1]).intValue();
            
            Game game = games.get(gameId);
            if (game == null) continue;
            
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id", game.getId());
            gameData.put("title", game.getTitle());
            gameData.put("coverUrl", game.getCoverImageUrl());
            gameData.put("playtime", minutes);
            
            mostPlayed.add(gameData);
            count++;
        }
        
        return mostPlayed;
    }
}