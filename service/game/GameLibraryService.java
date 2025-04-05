package com.example.demo.service.game;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.model.Game;
import com.example.demo.model.Library;
import com.example.demo.model.User;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.LibraryRepository;
import com.example.demo.repository.UserRepository;

@Service
public class GameLibraryService {
    
    private final LibraryRepository libraryRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
      
    public GameLibraryService(LibraryRepository libraryRepository, 
                             GameRepository gameRepository,
                             UserRepository userRepository) {
        this.libraryRepository = libraryRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }
    
    public List<Map<String, Object>> getUserLibrary(Long userId) {
        List<Library> libraryEntries = libraryRepository.findByUserId(userId);
        
        if (libraryEntries.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Get all game IDs from library
        List<Long> gameIds = libraryEntries.stream()
            .map(library -> library.getGame().getId())
            .collect(Collectors.toList());
        
        // Get all games in library
        Map<Long, Game> gameMap = gameRepository.findAllById(gameIds).stream()
            .collect(Collectors.toMap(Game::getId, game -> game));
        
        // Build response with game details and library metadata
        return libraryEntries.stream().map(library -> {
            Game game = gameMap.get(library.getGame().getId());
            if (game == null) return null;
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", library.getId());
            entry.put("gameId", game.getId());
            entry.put("title", game.getTitle());
            entry.put("coverImageUrl", game.getCoverImageUrl());
            entry.put("playtime", library.getPlaytime());
            entry.put("lastPlayed", library.getLastPlayed());
            entry.put("installed", library.isInstalled());
            entry.put("favorite", library.isFavorite());
            entry.put("hidden", library.isHidden());
            entry.put("category", library.getCategory());
            entry.put("purchaseDate", library.getPurchaseDate());
            
            return entry;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getRecentlyPlayedGames(Long userId) {
        List<Library> libraryEntries = libraryRepository.findByUserIdOrderByLastPlayedDesc(userId);
        
        // Get entries with lastPlayed not null
        List<Library> recentlyPlayed = libraryEntries.stream()
            .filter(library -> library.getLastPlayed() != null)
            .limit(5)
            .collect(Collectors.toList());
        
        // Get all game IDs from recent played
        List<Long> gameIds = recentlyPlayed.stream()
            .map(library -> library.getGame().getId())
            .collect(Collectors.toList());
        
        // Get all games in recently played
        Map<Long, Game> gameMap = gameRepository.findAllById(gameIds).stream()
            .collect(Collectors.toMap(Game::getId, game -> game));
        
        // Build response with game details and library metadata
        return recentlyPlayed.stream().map(library -> {
            Game game = gameMap.get(library.getGame().getId());
            if (game == null) return null;
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", library.getId());
            entry.put("gameId", game.getId());
            entry.put("title", game.getTitle());
            entry.put("coverImageUrl", game.getCoverImageUrl());
            entry.put("playtime", library.getPlaytime());
            entry.put("lastPlayed", library.getLastPlayed());
            
            return entry;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getFavoriteGames(Long userId) {
        List<Library> favorites = libraryRepository.findFavoritesByUserId(userId);
        
        // Get all game IDs from favorites
        List<Long> gameIds = favorites.stream()
            .map(library -> library.getGame().getId())
            .collect(Collectors.toList());
        
        // Get all games in favorites
        Map<Long, Game> gameMap = gameRepository.findAllById(gameIds).stream()
            .collect(Collectors.toMap(Game::getId, game -> game));
        
        // Build response with game details and library metadata
        return favorites.stream().map(library -> {
            Game game = gameMap.get(library.getGame().getId());
            if (game == null) return null;
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", library.getId());
            entry.put("gameId", game.getId());
            entry.put("title", game.getTitle());
            entry.put("coverImageUrl", game.getCoverImageUrl());
            entry.put("playtime", library.getPlaytime());
            
            return entry;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    public Library addGameToLibrary(Long userId, Long gameId) {
        // Check if user already has the game
        Optional<Library> existingEntry = libraryRepository.findByUserIdAndGameId(userId, gameId);
        if (existingEntry.isPresent()) {
            throw new IllegalStateException("Game already in user's library");
        }
        
        // Get user and game
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Create new library entry
        Library library = new Library();
        library.setUser(user);
        library.setGame(game);
        library.setPurchaseDate(LocalDateTime.now());
        library.setPlaytime(0);
        library.setInstalled(false);
        library.setFavorite(false);
        library.setHidden(false);
        
        return libraryRepository.save(library);
    }
    
    public void recordGamePlay(Long userId, Long gameId, int minutes) {
        Library library = libraryRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not in user's library"));
        
        library.addPlaytime(minutes);
        libraryRepository.save(library);
    }
    
    public void setGameInstalled(Long userId, Long gameId, boolean installed) {
        Library library = libraryRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not in user's library"));
        
        library.setInstalled(installed);
        libraryRepository.save(library);
    }
    
    public void toggleFavorite(Long userId, Long gameId) {
        Library library = libraryRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not in user's library"));
        
        library.setFavorite(!library.isFavorite());
        libraryRepository.save(library);
    }
    
    public void toggleHidden(Long userId, Long gameId) {
        Library library = libraryRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not in user's library"));
        
        library.setHidden(!library.isHidden());
        libraryRepository.save(library);
    }
    
    public void setGameCategory(Long userId, Long gameId, String category) {
        Library library = libraryRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not in user's library"));
        
        library.setCategory(category);
        libraryRepository.save(library);
    }
    
    public void removeFromLibrary(Long userId, Long gameId) {
        Library library = libraryRepository.findByUserIdAndGameId(userId, gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not in user's library"));
        
        libraryRepository.delete(library);
    }
    
    public int getLibrarySize(Long userId) {
        return libraryRepository.countGamesByUserId(userId);
    }
    
    public int getTotalPlaytime(Long userId) {
        Integer playtime = libraryRepository.getTotalPlaytimeByUserId(userId);
        return playtime != null ? playtime : 0;
    }
    
    public boolean hasGame(Long userId, Long gameId) {
        return libraryRepository.existsByUserIdAndGameId(userId, gameId);
    }
}