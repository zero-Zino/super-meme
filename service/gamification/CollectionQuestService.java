package com.example.demo.service.gamification;

import com.example.demo.model.CollectionQuest;
import com.example.demo.model.Game;
import com.example.demo.model.Library;
import com.example.demo.repository.CollectionQuestRepository;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.LibraryRepository;
import com.example.demo.service.realtime.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollectionQuestService {
    
    private final CollectionQuestRepository questRepository;
    private final LibraryRepository libraryRepository;
    private final GameRepository gameRepository;
    private final NotificationService notificationService;
    
    // Predefined collection quests
    private final List<Map<String, Object>> COLLECTION_QUESTS = Arrays.asList(
        Map.of(
            "id", "indie_gems",
            "name", "Indie Gems Collector",
            "description", "Collect 5 indie games in your library",
            "targetCount", 5,
            "genre", "indie",
            "points", 30,
            "badgeUrl", "badges/indie_gems.png"
        ),
        Map.of(
            "id", "rpg_enthusiast",
            "name", "RPG Enthusiast",
            "description", "Add 3 RPG games to your collection",
            "targetCount", 3,
            "genre", "rpg",
            "points", 25,
            "badgeUrl", "badges/rpg_enthusiast.png"
        ),
        Map.of(
            "id", "strategy_master",
            "name", "Strategy Master",
            "description", "Own 4 strategy games",
            "targetCount", 4,
            "genre", "strategy",
            "points", 35,
            "badgeUrl", "badges/strategy_master.png"
        ),
        Map.of(
            "id", "action_packed",
            "name", "Action Packed",
            "description", "Fill your library with 6 action games",
            "targetCount", 6,
            "genre", "action",
            "points", 40,
            "badgeUrl", "badges/action_packed.png"
        ),
        Map.of(
            "id", "simulation_fan",
            "name", "Simulation Fan",
            "description", "Collect 3 simulation games",
            "targetCount", 3,
            "genre", "simulation",
            "points", 25,
            "badgeUrl", "badges/simulation_fan.png"
        ),
        Map.of(
            "id", "sports_collection",
            "name", "Sports Collection",
            "description", "Acquire 3 sports games",
            "targetCount", 3,
            "genre", "sports",
            "points", 25,
            "badgeUrl", "badges/sports_collection.png"
        ),
        Map.of(
            "id", "horror_survivor",
            "name", "Horror Survivor",
            "description", "Brave enough to own 3 horror games",
            "targetCount", 3,
            "genre", "horror",
            "points", 30,
            "badgeUrl", "badges/horror_survivor.png"
        ),
        Map.of(
            "id", "adventure_seeker",
            "name", "Adventure Seeker",
            "description", "Journey with 5 adventure games",
            "targetCount", 5,
            "genre", "adventure",
            "points", 35,
            "badgeUrl", "badges/adventure_seeker.png"
        ),
        Map.of(
            "id", "racing_enthusiast",
            "name", "Racing Enthusiast",
            "description", "Speed through with 3 racing games",
            "targetCount", 3,
            "genre", "racing",
            "points", 25,
            "badgeUrl", "badges/racing_enthusiast.png"
        ),
        Map.of(
            "id", "game_connoisseur",
            "name", "Game Connoisseur",
            "description", "Own games from 8 different genres",
            "targetCount", 8,
            "genre", "mixed",
            "points", 50,
            "badgeUrl", "badges/game_connoisseur.png"
        )
    );
    
    @Autowired
    public CollectionQuestService(
            CollectionQuestRepository questRepository,
            LibraryRepository libraryRepository,
            GameRepository gameRepository,
            NotificationService notificationService) {
        this.questRepository = questRepository;
        this.libraryRepository = libraryRepository;
        this.gameRepository = gameRepository;
        this.notificationService = notificationService;
    }
    
    public List<Map<String, Object>> getAllQuests() {
        return COLLECTION_QUESTS;
    }
    
    public List<Map<String, Object>> getUserQuests(Long userId) {
        // Get all quests a user has started or completed
        List<CollectionQuest> userQuests = questRepository.findByUserId(userId);
        
        // Create a map of quest IDs to their progress
        Map<String, CollectionQuest> userQuestMap = userQuests.stream()
            .collect(Collectors.toMap(CollectionQuest::getQuestId, q -> q));
        
        // Prepare result with all quests and their status for this user
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map<String, Object> quest : COLLECTION_QUESTS) {
            String questId = (String) quest.get("id");
            CollectionQuest userQuest = userQuestMap.get(questId);
            
            Map<String, Object> questData = new HashMap<>(quest);
            
            if (userQuest != null) {
                questData.put("started", true);
                questData.put("currentCount", userQuest.getCurrentCount());
                questData.put("completed", userQuest.isCompleted());
                
                if (userQuest.isCompleted()) {
                    questData.put("completedDate", userQuest.getCompletedDate().toString());
                }
            } else {
                questData.put("started", false);
                questData.put("currentCount", 0);
                questData.put("completed", false);
            }
            
            result.add(questData);
        }
        
        return result;
    }
    
    public void checkQuestProgress(Long userId) {
        // Get user's library
        List<Library> userLibrary = libraryRepository.findByUserId(userId);
        
        if (userLibrary.isEmpty()) {
            return; // No games to check
        }
        
        // Get game IDs from library
        List<Long> gameIds = userLibrary.stream()
            .map(Library::getGameId)
            .collect(Collectors.toList());
        
        // Get game details
        List<Game> games = gameRepository.findAllById(gameIds);
        
        // Count games by genre
        Map<String, Integer> genreCounts = new HashMap<>();
        Set<String> uniqueGenres = new HashSet<>();
        
        for (Game game : games) {
            for (String genre : game.getGenres()) {
                genreCounts.put(genre.toLowerCase(), genreCounts.getOrDefault(genre.toLowerCase(), 0) + 1);
                uniqueGenres.add(genre.toLowerCase());
            }
        }
        
        // Check each quest
        for (Map<String, Object> questData : COLLECTION_QUESTS) {
            String questId = (String) questData.get("id");
            String genre = (String) questData.get("genre");
            int targetCount = (int) questData.get("targetCount");
            
            // Special case for "mixed" genre (game_connoisseur quest)
            int currentCount;
            if ("mixed".equals(genre)) {
                currentCount = uniqueGenres.size();
            } else {
                currentCount = genreCounts.getOrDefault(genre, 0);
            }
            
            // Update or create quest progress
            CollectionQuest quest = questRepository.findByUserIdAndQuestId(userId, questId)
                .orElseGet(() -> {
                    CollectionQuest newQuest = new CollectionQuest();
                    newQuest.setUserId(userId);
                    newQuest.setQuestId(questId);
                    newQuest.setStartDate(LocalDateTime.now());
                    newQuest.setCompleted(false);
                    return newQuest;
                });
            
            // Update count
            quest.setCurrentCount(currentCount);
            
            // Check for completion
            if (currentCount >= targetCount && !quest.isCompleted()) {
                quest.setCompleted(true);
                quest.setCompletedDate(LocalDateTime.now());
                
                // Send notification for quest completion
                notificationService.sendNotification(
                    userId,
                    "Collection Quest Completed",
                    "You've completed the " + questData.get("name") + " quest!",
                    "collection_quest_completed",
                    Map.of("questId", questId)
                );
            }
            
            questRepository.save(quest);
        }
    }
    
    public Map<String, Object> getQuestDetails(String questId) {
        return COLLECTION_QUESTS.stream()
            .filter(q -> q.get("id").equals(questId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Quest not found"));
    }
    
    public List<Map<String, Object>> getRecommendedGamesForQuest(Long userId, String questId) {
        // Get quest details
        Map<String, Object> quest = getQuestDetails(questId);
        String targetGenre = (String) quest.get("genre");
        
        // Special case for mixed genre
        if ("mixed".equals(targetGenre)) {
            // Find genres the user doesn't have yet
            // Implementation would depend on specific requirements
            return Collections.emptyList();
        }
        
        // Get user's library
        List<Long> userGameIds = libraryRepository.findByUserId(userId).stream()
            .map(Library::getGameId)
            .collect(Collectors.toList());
        
        // Find games of the target genre that the user doesn't own
        List<Game> genreGames = gameRepository.findByGenre(targetGenre, null).getContent();
        List<Game> recommendedGames = genreGames.stream()
            .filter(game -> !userGameIds.contains(game.getId()))
            .limit(5) // Limit to 5 recommendations
            .collect(Collectors.toList());
        
        // Convert to response format
        return recommendedGames.stream()
            .map(game -> {
                Map<String, Object> gameData = new HashMap<>();
                gameData.put("id", game.getId());
                gameData.put("title", game.getTitle());
                gameData.put("coverUrl", game.getCoverImageUrl());
                gameData.put("price", game.getPrice());
                gameData.put("onSale", game.isOnSale());
                
                if (game.isOnSale()) {
                    gameData.put("salePrice", game.getSalePrice());
                }
                
                return gameData;
            })
            .collect(Collectors.toList());
    }
    
    public int getCompletedQuestCount(Long userId) {
        return questRepository.countCompletedQuestsByUserId(userId);
    }
}