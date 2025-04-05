package com.example.demo.service.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.model.Game;
import com.example.demo.repository.GameRepository;


@Service
public class GameService {
    
    private final GameRepository gameRepository;
    

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    
    public Page<Game> getAllGames(Pageable pageable) {
        return gameRepository.findAll(pageable);
    }
    
    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }
    
    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }
    
    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }
    
    public Page<Game> searchGames(String query, Pageable pageable) {
        return gameRepository.findByTitleContainingIgnoreCase(query, pageable);
    }
    
    public Page<Game> getGamesByGenre(String genre, Pageable pageable) {
        return gameRepository.findByGenre(genre, pageable);
    }
    
    public Page<Game> getGamesByTag(String tag, Pageable pageable) {
        return gameRepository.findByTag(tag, pageable);
    }
    
    public List<Game> getFeaturedGames() {
        return gameRepository.findByFeaturedTrue();
    }
    
    public List<Game> getGamesOnSale() {
        return gameRepository.findByOnSaleTrue();
    }
    
    public List<Game> getNewReleases(int limit) {
        return gameRepository.findNewReleases(org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    public List<Game> getTopRatedGames(int limit) {
        return gameRepository.findTopRated(org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    public List<Game> getTopRatedByGenre(String genre, int limit) {
        return gameRepository.findTopRatedByGenre(genre, org.springframework.data.domain.PageRequest.of(0, limit));
    }
    
    public List<Game> getRecommendedGames(Long userId, int limit) {
        // This would typically use a recommendation algorithm based on user preferences
        // For now, we'll return top rated games
        return getTopRatedGames(limit);
    }
    
    public Map<String, Object> getSystemRequirements(Long gameId) {
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        
        if (gameOpt.isEmpty()) {
            return null;
        }
        
        Game game = gameOpt.get();
        String requirementsString = game.getSystemRequirements();
        
        // Parse requirements string into structured data
        // This assumes a specific format; you might need to adjust based on your actual data
        try {
            Map<String, Object> requirements = new HashMap<>();
            
            // For a simple implementation, just return the raw string
            requirements.put("raw", requirementsString);
            
            // In a real implementation, you would parse this into more detailed structures
            // For example:
            requirements.put("minimum", Map.of(
                "os", "Windows 10",
                "processor", "Intel Core i5",
                "memory", "8 GB RAM",
                "graphics", "NVIDIA GeForce GTX 1060",
                "storage", "50 GB available space"
            ));
            
            requirements.put("recommended", Map.of(
                "os", "Windows 10",
                "processor", "Intel Core i7",
                "memory", "16 GB RAM",
                "graphics", "NVIDIA GeForce RTX 2060",
                "storage", "50 GB available space"
            ));
            
            return requirements;
        } catch (Exception e) {
            // If parsing fails, return the raw string
            return Map.of("raw", requirementsString);
        }
    }
    
    public void updateGameRating(Long gameId, double rating) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Update average rating
        double currentRating = game.getAverageRating();
        int reviewCount = game.getReviewCount();
        
        double newRating = ((currentRating * reviewCount) + rating) / (reviewCount + 1);
        
        game.setAverageRating(newRating);
        game.setReviewCount(reviewCount + 1);
        
        gameRepository.save(game);
    }
    
    public void setGameOnSale(Long gameId, double salePrice, boolean onSale) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        game.setSalePrice(salePrice);
        game.setOnSale(onSale);
        
        gameRepository.save(game);
    }
    
    public void addGameGenre(Long gameId, String genre) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        List<String> genres = game.getGenres();
        if (!genres.contains(genre)) {
            genres.add(genre);
            game.setGenres(genres);
            gameRepository.save(game);
        }
    }
    
    public void addGameTag(Long gameId, String tag) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        List<String> tags = game.getTags();
        if (!tags.contains(tag)) {
            tags.add(tag);
            game.setTags(tags);
            gameRepository.save(game);
        }
    }
    
    public void addGameScreenshot(Long gameId, String screenshotUrl) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        List<String> screenshots = game.getScreenshots();
        if (!screenshots.contains(screenshotUrl)) {
            screenshots.add(screenshotUrl);
            game.setScreenshots(screenshots);
            gameRepository.save(game);
        }
    }
    
    public Page<Game> getGamesByMaxPrice(double maxPrice, Pageable pageable) {
        return gameRepository.findByMaxPrice(maxPrice, pageable);
    }
    
    public Page<Game> getGamesByGenreAndMaxPrice(String genre, double maxPrice, Pageable pageable) {
        return gameRepository.findByGenreAndMaxPrice(genre, maxPrice, pageable);
    }
}