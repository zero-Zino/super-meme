package com.example.demo.service.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.model.Game;
import com.example.demo.repository.GameRepository;
import com.example.demo.service.integration.SteamAPIService;

@Service
public class GameMetadataService {
    
    private final GameRepository gameRepository;
    private final SteamAPIService steamAPIService;
    

    public GameMetadataService(GameRepository gameRepository, SteamAPIService steamAPIService) {
        this.gameRepository = gameRepository;
        this.steamAPIService = steamAPIService;
    }
    
    public void updateGameMetadata(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Call SteamAPIService to get updated metadata
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> metadata = steamAPIService.getGameDetails(game.getTitle());
                updateGameFromMetadata(game, metadata);
            } catch (Exception e) {
                // Log error but don't fail
                System.err.println("Error updating metadata for game " + gameId + ": " + e.getMessage());
            }
        });
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    public void updateFeaturedGamesMetadata() {
        List<Game> featured = gameRepository.findByFeaturedTrue();
        
        for (Game game : featured) {
            updateGameMetadata(game.getId());
        }
    }
    
    @Scheduled(cron = "0 0 3 * * ?") // Run at 3 AM every day
    public void updateNewReleasesMetadata() {
        List<Game> newReleases = gameRepository.findNewReleases(org.springframework.data.domain.PageRequest.of(0, 20));
        
        for (Game game : newReleases) {
            updateGameMetadata(game.getId());
        }
    }
    
    private void updateGameFromMetadata(Game game, Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return;
        }
        
        // Update fields based on available metadata
        if (metadata.containsKey("description")) {
            game.setDescription((String) metadata.get("description"));
        }
        
        if (metadata.containsKey("system_requirements")) {
            game.setSystemRequirements((String) metadata.get("system_requirements"));
        }
        
        if (metadata.containsKey("screenshots") && metadata.get("screenshots") instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> screenshots = (List<String>) metadata.get("screenshots");
            game.setScreenshots(screenshots);
        }
        
        if (metadata.containsKey("price")) {
            Object priceObj = metadata.get("price");
            if (priceObj instanceof Number number) {
                game.setPrice(number.doubleValue());
            }
        }
        
        if (metadata.containsKey("genres") && metadata.get("genres") instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> genres = (List<String>) metadata.get("genres");
            game.setGenres(genres);
        }
        
        if (metadata.containsKey("tags") && metadata.get("tags") instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) metadata.get("tags");
            game.setTags(tags);
        }
        
        // Save updated game
        gameRepository.save(game);
    }
    
    public Map<String, Object> enrichGameDetails(Map<String, Object> gameDetails) {
        // Add additional metadata to game details
        Long gameId = (Long) gameDetails.get("id");
        
        // Example: Add similar games
        List<Game> similarGames = findSimilarGames(gameId);
        List<Map<String, Object>> similarGamesList = similarGames.stream()
            .map(game -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", game.getId());
                data.put("title", game.getTitle());
                data.put("coverImageUrl", game.getCoverImageUrl());
                return data;
            })
            .collect(Collectors.toList());
        
        gameDetails.put("similarGames", similarGamesList);
        
        // Add popularity score
        gameDetails.put("popularityScore", calculatePopularityScore(gameId));
        
        return gameDetails;
    }
    
    private List<Game> findSimilarGames(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Get primary genre for the game
        List<String> genres = game.getGenres();
        if (genres == null || genres.isEmpty()) {
            return Collections.emptyList();
        }
        
        String primaryGenre = genres.get(0);
        
        // Find games in the same genre, excluding this game
        Page<Game> genreGames = gameRepository.findByGenre(
            primaryGenre, 
            org.springframework.data.domain.PageRequest.of(0, 6)
        );
        
        // Filter out the current game and limit to 5 similar games
        return genreGames.stream()
            .filter(g -> !g.getId().equals(gameId))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    private int calculatePopularityScore(@SuppressWarnings("unused") Long gameId) {
        // This would be based on various factors like sales, playtime, etc.
        // For now, we'll return a random score between 1-100
        return new Random().nextInt(100) + 1;
    }
}