package com.example.demo.service.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.model.Game;
import com.example.demo.model.Library;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.LibraryRepository;

@Service
public class GameShelfService {
    
    private final LibraryRepository libraryRepository;
    private final GameRepository gameRepository;
    

    public GameShelfService(LibraryRepository libraryRepository, GameRepository gameRepository) {
        this.libraryRepository = libraryRepository;
        this.gameRepository = gameRepository;
    }
    
    public Map<String, Object> getUserGameShelf(Long userId) {
        // Get user's library
        List<Library> libraryEntries = libraryRepository.findByUserIdOrderByLastPlayedDesc(userId);
        
        if (libraryEntries.isEmpty()) {
            return new HashMap<>();
        }
        
        // Get all games in the library
        List<Long> gameIds = libraryEntries.stream()
            .map(Library::getGameId)
            .collect(Collectors.toList());
        
        List<Game> games = gameRepository.findAllById(gameIds);
        
        // Map games to their library entries
        Map<Long, Game> gameMap = games.stream()
            .collect(Collectors.toMap(Game::getId, game -> game));
        
        // Create 3D shelf representation
        Map<String, Object> shelfData = new HashMap<>();
        
        // Create shelves by category/genre
        Map<String, List<Map<String, Object>>> shelvesByGenre = new HashMap<>();
        
        for (Library entry : libraryEntries) {
            Game game = gameMap.get(entry.getGameId());
            if (game == null) continue;
            
            String primaryGenre = getPrimaryGenre(game);
            
            Map<String, Object> gameObj = new HashMap<>();
            gameObj.put("id", game.getId());
            gameObj.put("title", game.getTitle());
            gameObj.put("coverUrl", game.getCoverImageUrl());
            gameObj.put("playtime", entry.getPlaytime());
            gameObj.put("lastPlayed", entry.getLastPlayed());
            gameObj.put("favorite", entry.isFavorite());
            gameObj.put("installed", entry.isInstalled());
            
            // Add to appropriate shelf
            if (!shelvesByGenre.containsKey(primaryGenre)) {
                shelvesByGenre.put(primaryGenre, new ArrayList<>());
            }
            shelvesByGenre.get(primaryGenre).add(gameObj);
        }
        
        // Sort shelves for better 3D organization
        List<Map<String, Object>> organizedShelves = new ArrayList<>();
        shelvesByGenre.forEach((genre, games3d) -> {
            Map<String, Object> shelf = new HashMap<>();
            shelf.put("genre", genre);
            shelf.put("games", games3d);
            organizedShelves.add(shelf);
        });
        
        shelfData.put("shelves", organizedShelves);
        shelfData.put("totalGames", gameIds.size());
        
        // Add recently played section
        List<Map<String, Object>> recentlyPlayed = libraryEntries.stream()
            .filter(entry -> entry.getLastPlayed() != null)
            .sorted(Comparator.comparing(Library::getLastPlayed).reversed())
            .limit(5)
            .map(entry -> {
                Game game = gameMap.get(entry.getGameId());
                if (game == null) return null;
                
                Map<String, Object> gameObj = new HashMap<>();
                gameObj.put("id", game.getId());
                gameObj.put("title", game.getTitle());
                gameObj.put("coverUrl", game.getCoverImageUrl());
                gameObj.put("playtime", entry.getPlaytime());
                gameObj.put("lastPlayed", entry.getLastPlayed());
                return gameObj;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        shelfData.put("recentlyPlayed", recentlyPlayed);
        
        // Add favorite section
        List<Map<String, Object>> favorites = libraryEntries.stream()
            .filter(Library::isFavorite)
            .map(entry -> {
                Game game = gameMap.get(entry.getGameId());
                if (game == null) return null;
                
                Map<String, Object> gameObj = new HashMap<>();
                gameObj.put("id", game.getId());
                gameObj.put("title", game.getTitle());
                gameObj.put("coverUrl", game.getCoverImageUrl());
                gameObj.put("playtime", entry.getPlaytime());
                return gameObj;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        shelfData.put("favorites", favorites);
        
        // Add metadata for 3D rendering
        shelfData.put("shelfLayout", getDefaultShelfLayout());
        
        return shelfData;
    }
    
    private String getPrimaryGenre(Game game) {
        // Assuming game.getGenres() returns a list or array of genres
        List<String> genres = game.getGenres();
        if (genres == null || genres.isEmpty()) {
            return "Uncategorized";
        }
        return genres.get(0);
    }
    
    public void updateGameShelfOrganization(Long userId, Map<String, List<Long>> shelfOrganization) {
        // Update user's custom shelf organization
        // This could be storing custom shelf layouts, categorizations, etc.
        // Implementation depends on specific UI requirements
    }
    
    private Map<String, Object> getDefaultShelfLayout() {
        Map<String, Object> layout = new HashMap<>();
        
        // Define shelf layout parameters for 3D rendering
        layout.put("shelfDepth", 0.5);
        layout.put("shelfHeight", 0.2);
        layout.put("shelfSpacing", 0.5);
        layout.put("gameSpacing", 0.1);
        layout.put("shelfColor", "#8B4513"); // Brown wood color
        layout.put("backgroundColor", "#1B2838"); // Steam dark blue
        layout.put("lightIntensity", 0.7);
        layout.put("cameraPosition", Arrays.asList(0, 1.5, 4));
        layout.put("cameraTarget", Arrays.asList(0, 0, 0));
        
        // Define animations
        Map<String, Object> animations = new HashMap<>();
        animations.put("rotationSpeed", 0.2);
        animations.put("hoverScale", 1.2);
        animations.put("transitionDuration", 0.5);
        layout.put("animations", animations);
        
        return layout;
    }
    
    public Map<String, Object> getGameShelfDetails(Long userId, Long gameId) {
        // Get specific game details for the 3D shelf
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        Optional<Library> libraryEntry = libraryRepository.findByUserIdAndGameId(userId, gameId);
        
        Map<String, Object> details = new HashMap<>();
        details.put("id", game.getId());
        details.put("title", game.getTitle());
        details.put("coverUrl", game.getCoverImageUrl());
        details.put("description", game.getDescription());
        details.put("developer", game.getDeveloper());
        details.put("publisher", game.getPublisher());
        details.put("releaseDate", game.getReleaseDate());
        details.put("genres", game.getGenres());
        
        // Add library-specific information if available
        if (libraryEntry.isPresent()) {
            Library entry = libraryEntry.get();
            details.put("playtime", entry.getPlaytime());
            details.put("lastPlayed", entry.getLastPlayed());
            details.put("installed", entry.isInstalled());
            details.put("favorite", entry.isFavorite());
        }
        
        // Add 3D model details
        details.put("modelUrl", "models/game_box.obj"); // Default model URL
        details.put("textureUrl", game.getCoverImageUrl());
        details.put("scale", Arrays.asList(1, 1.5, 0.2)); // Standard game box scale
        
        return details;
    }
}