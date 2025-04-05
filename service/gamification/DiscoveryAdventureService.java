package com.example.demo.service.gamification;

import com.example.demo.model.DiscoveryAdventure;
import com.example.demo.model.Game;
import com.example.demo.model.Library;
import com.example.demo.repository.DiscoveryAdventureRepository;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.LibraryRepository;
import com.example.demo.service.realtime.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscoveryAdventureService {
    
    private final DiscoveryAdventureRepository adventureRepository;
    private final GameRepository gameRepository;
    private final LibraryRepository libraryRepository;
    private final NotificationService notificationService;
    
    // Predefined discovery adventures
    private final List<Map<String, Object>> DISCOVERY_ADVENTURES = Arrays.asList(
        Map.of(
            "id", "indie_exploration",
            "name", "Indie Game Exploration",
            "description", "Discover the creative world of indie games",
            "genre", "indie",
            "steps", 3,
            "points", 40,
            "iconUrl", "adventures/indie_exploration.png"
        ),
        Map.of(
            "id", "rpg_journey",
            "name", "RPG Journey",
            "description", "Embark on epic role-playing adventures",
            "genre", "rpg",
            "steps", 3,
            "points", 45,
            "iconUrl", "adventures/rpg_journey.png"
        ),
        Map.of(
            "id", "strategy_expedition",
            "name", "Strategy Expedition",
            "description", "Test your tactical mind with strategy games",
            "genre", "strategy",
            "steps", 3,
            "points", 40,
            "iconUrl", "adventures/strategy_expedition.png"
        ),
        Map.of(
            "id", "horror_descent",
            "name", "Horror Descent",
            "description", "Brave the terrifying world of horror games",
            "genre", "horror",
            "steps", 3,
            "points", 50,
            "iconUrl", "adventures/horror_descent.png"
        ),
        Map.of(
            "id", "simulation_immersion",
            "name", "Simulation Immersion",
            "description", "Experience life through simulation games",
            "genre", "simulation",
            "steps", 3,
            "points", 40,
            "iconUrl", "adventures/simulation_immersion.png"
        ),
        Map.of(
            "id", "action_odyssey",
            "name", "Action Odyssey",
            "description", "Get your adrenaline pumping with action games",
            "genre", "action",
            "steps", 3,
            "points", 45,
            "iconUrl", "adventures/action_odyssey.png"
        ),
        Map.of(
            "id", "adventure_quest",
            "name", "Adventure Quest",
            "description", "Explore worlds through adventure games",
            "genre", "adventure",
            "steps", 3,
            "points", 40,
            "iconUrl", "adventures/adventure_quest.png"
        )
    );
    
    @Autowired
    public DiscoveryAdventureService(
            DiscoveryAdventureRepository adventureRepository,
            GameRepository gameRepository,
            LibraryRepository libraryRepository,
            NotificationService notificationService) {
        this.adventureRepository = adventureRepository;
        this.gameRepository = gameRepository;
        this.libraryRepository = libraryRepository;
        this.notificationService = notificationService;
    }
    
    public List<Map<String, Object>> getAllAdventures() {
        return DISCOVERY_ADVENTURES;
    }
    
    public List<Map<String, Object>> getUserAdventures(Long userId) {
        // Get all adventures a user has started
        List<DiscoveryAdventure> userAdventures = adventureRepository.findByUserId(userId);
        
        // Map of adventure IDs to user's progress
        Map<String, DiscoveryAdventure> userAdventureMap = userAdventures.stream()
            .collect(Collectors.toMap(DiscoveryAdventure::getAdventureId, a -> a));
        
        // Build result
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> adventure : DISCOVERY_ADVENTURES) {
            String adventureId = (String) adventure.get("id");
            DiscoveryAdventure userAdventure = userAdventureMap.get(adventureId);
            
            Map<String, Object> adventureData = new HashMap<>(adventure);
            
            if (userAdventure != null) {
                adventureData.put("started", true);
                adventureData.put("currentStep", userAdventure.getCurrentStep());
                adventureData.put("completed", userAdventure.isCompleted());
                adventureData.put("gameIds", userAdventure.getCompletedGameIds());
                
                if (userAdventure.isCompleted()) {
                    adventureData.put("completedDate", userAdventure.getCompletedDate().toString());
                }
                
                // Get current game if in progress
                if (!userAdventure.isCompleted() && userAdventure.getCurrentGameId() != null) {
                    Game currentGame = gameRepository.findById(userAdventure.getCurrentGameId()).orElse(null);
                    if (currentGame != null) {
                        Map<String, Object> gameData = new HashMap<>();
                        gameData.put("id", currentGame.getId());
                        gameData.put("title", currentGame.getTitle());
                        gameData.put("coverUrl", currentGame.getCoverImageUrl());
                        
                        adventureData.put("currentGame", gameData);
                    }
                }
            } else {
                adventureData.put("started", false);
                adventureData.put("currentStep", 0);
                adventureData.put("completed", false);
            }
            
            result.add(adventureData);
        }
        
        return result;
    }
    
    public Map<String, Object> startAdventure(Long userId, String adventureId) {
        // Verify adventure exists
        Map<String, Object> adventureData = DISCOVERY_ADVENTURES.stream()
            .filter(a -> a.get("id").equals(adventureId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Adventure not found"));
        
        // Check if already started
        Optional<DiscoveryAdventure> existingAdventure = 
            adventureRepository.findByUserIdAndAdventureId(userId, adventureId);
        
        if (existingAdventure.isPresent()) {
            throw new IllegalStateException("Adventure already started");
        }
        
        // Get genre for this adventure
        String genre = (String) adventureData.get("genre");
        
        // Get user's library
        List<Long> userGameIds = libraryRepository.findByUserId(userId).stream()
            .map(Library::getGameId)
            .collect(Collectors.toList());
        
        // Find a game for the first step
        List<Game> genreGames = gameRepository.findByGenre(genre, PageRequest.of(0, 20)).getContent();
        
        // Filter out games the user already owns
        List<Game> availableGames = genreGames.stream()
            .filter(game -> !userGameIds.contains(game.getId()))
            .collect(Collectors.toList());
        
        if (availableGames.isEmpty()) {
            throw new IllegalStateException("No available games found for this adventure");
        }
        
        // Select a random game
        Game selectedGame = availableGames.get(new Random().nextInt(availableGames.size()));
        
        // Create new adventure
        DiscoveryAdventure adventure = new DiscoveryAdventure();
        adventure.setUserId(userId);
        adventure.setAdventureId(adventureId);
        adventure.setStartDate(LocalDateTime.now());
        adventure.setCurrentStep(1);
        adventure.setCompleted(false);
        adventure.setCurrentGameId(selectedGame.getId());
        adventure.setCompletedGameIds(new ArrayList<>());
        
        adventureRepository.save(adventure);
        
        // Create response
        Map<String, Object> response = new HashMap<>(adventureData);
        response.put("started", true);
        response.put("currentStep", 1);
        response.put("completed", false);
        
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("id", selectedGame.getId());
        gameData.put("title", selectedGame.getTitle());
        gameData.put("coverUrl", selectedGame.getCoverImageUrl());
        gameData.put("price", selectedGame.getPrice());
        gameData.put("onSale", selectedGame.isOnSale());
        
        if (selectedGame.isOnSale()) {
            gameData.put("salePrice", selectedGame.getSalePrice());
        }
        
        response.put("currentGame", gameData);
        
        return response;
    }
    
    public Map<String, Object> progressAdventure(Long userId, String adventureId, Long gameId) {
        // Find user's adventure
        DiscoveryAdventure adventure = adventureRepository.findByUserIdAndAdventureId(userId, adventureId)
            .orElseThrow(() -> new IllegalArgumentException("Adventure not found"));
        
        // Verify this is the correct game
        if (!gameId.equals(adventure.getCurrentGameId())) {
            throw new IllegalArgumentException("Wrong game for current adventure step");
        }
        
        // Get adventure data
        Map<String, Object> adventureData = DISCOVERY_ADVENTURES.stream()
            .filter(a -> a.get("id").equals(adventureId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Adventure definition not found"));
        
        // Add to completed games
        List<Long> completedGames = adventure.getCompletedGameIds();
        completedGames.add(gameId);
        adventure.setCompletedGameIds(completedGames);
        
        int totalSteps = (int) adventureData.get("steps");
        
        // Check if adventure is complete
        if (adventure.getCurrentStep() >= totalSteps) {
            adventure.setCompleted(true);
            adventure.setCompletedDate(LocalDateTime.now());
            adventure.setCurrentGameId(null);
            
            // Send notification
            notificationService.sendNotification(
                userId,
                "Discovery Adventure Completed",
                "You've completed the " + adventureData.get("name") + " adventure!",
                "discovery_adventure_completed",
                Map.of("adventureId", adventureId)
            );
        } else {
            // Increment step
            adventure.setCurrentStep(adventure.getCurrentStep() + 1);
            
            // Select next game
            String genre = (String) adventureData.get("genre");
            
            // Get user's library and already completed games
            List<Long> userGameIds = libraryRepository.findByUserId(userId).stream()
                .map(Library::getGameId)
                .collect(Collectors.toList());
            
            // Combine with already completed games in this adventure
            List<Long> excludeGameIds = new ArrayList<>(userGameIds);
            excludeGameIds.addAll(completedGames);
            
            // Find games for the genre, excluding ones the user owns or has already used
            List<Game> genreGames = gameRepository.findByGenre(genre, PageRequest.of(0, 20)).getContent();
            List<Game> availableGames = genreGames.stream()
                .filter(game -> !excludeGameIds.contains(game.getId()))
                .collect(Collectors.toList());
            
            if (availableGames.isEmpty()) {
                // If no more are available, allow repeats but not from user's library
                availableGames = genreGames.stream()
                    .filter(game -> !userGameIds.contains(game.getId()))
                    .collect(Collectors.toList());
            }
            
            if (!availableGames.isEmpty()) {
                Game nextGame = availableGames.get(new Random().nextInt(availableGames.size()));
                adventure.setCurrentGameId(nextGame.getId());
            } else {
                // No available games, mark as complete
                adventure.setCompleted(true);
                adventure.setCompletedDate(LocalDateTime.now());
                adventure.setCurrentGameId(null);
            }
        }
        
        adventureRepository.save(adventure);
        
        // Build response
        Map<String, Object> response = new HashMap<>(adventureData);
        response.put("started", true);
        response.put("currentStep", adventure.getCurrentStep());
        response.put("completed", adventure.isCompleted());
        response.put("gameIds", adventure.getCompletedGameIds());
        
        if (adventure.isCompleted()) {
            response.put("completedDate", adventure.getCompletedDate().toString());
        } else if (adventure.getCurrentGameId() != null) {
            // Get current game details
            Game currentGame = gameRepository.findById(adventure.getCurrentGameId()).orElse(null);
            if (currentGame != null) {
                Map<String, Object> gameData = new HashMap<>();
                gameData.put("id", currentGame.getId());
                gameData.put("title", currentGame.getTitle());
                gameData.put("coverUrl", currentGame.getCoverImageUrl());
                gameData.put("price", currentGame.getPrice());
                gameData.put("onSale", currentGame.isOnSale());
                
                if (currentGame.isOnSale()) {
                    gameData.put("salePrice", currentGame.getSalePrice());
                }
                
                response.put("currentGame", gameData);
            }
        }
        
        return response;
    }
    
    public void skipCurrentGame(Long userId, String adventureId) {
        // Find user's adventure
        DiscoveryAdventure adventure = adventureRepository.findByUserIdAndAdventureId(userId, adventureId)
            .orElseThrow(() -> new IllegalArgumentException("Adventure not found"));
        
        if (adventure.isCompleted()) {
            throw new IllegalStateException("Adventure is already completed");
        }
        
        // Get adventure data
        Map<String, Object> adventureData = DISCOVERY_ADVENTURES.stream()
            .filter(a -> a.get("id").equals(adventureId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Adventure definition not found"));
        
        // Select a new game
        String genre = (String) adventureData.get("genre");
        
        // Get user's library and already completed games
        List<Long> userGameIds = libraryRepository.findByUserId(userId).stream()
            .map(Library::getGameId)
            .collect(Collectors.toList());
        
        // Combine with already completed games in this adventure
        List<Long> excludeGameIds = new ArrayList<>(userGameIds);
        excludeGameIds.addAll(adventure.getCompletedGameIds());
        
        // Also exclude current game
        if (adventure.getCurrentGameId() != null) {
            excludeGameIds.add(adventure.getCurrentGameId());
        }
        
        // Find games for the genre, excluding ones the user owns or has already used
        List<Game> genreGames = gameRepository.findByGenre(genre, PageRequest.of(0, 20)).getContent();
        List<Game> availableGames = genreGames.stream()
            .filter(game -> !excludeGameIds.contains(game.getId()))
            .collect(Collectors.toList());
        
        if (availableGames.isEmpty()) {
            // If no more are available, allow repeats but not from user's library or current game
            availableGames = genreGames.stream()
                .filter(game -> !userGameIds.contains(game.getId()) && 
                                !game.getId().equals(adventure.getCurrentGameId()))
                .collect(Collectors.toList());
        }
        
        if (!availableGames.isEmpty()) {
            Game nextGame = availableGames.get(new Random().nextInt(availableGames.size()));
            adventure.setCurrentGameId(nextGame.getId());
            adventureRepository.save(adventure);
        } else {
            throw new IllegalStateException("No alternative games available");
        }
    }
    
    public int getCompletedAdventureCount(Long userId) {
        return adventureRepository.countCompletedAdventuresByUserId(userId);
    }
}