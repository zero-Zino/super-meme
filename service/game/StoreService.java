package com.example.demo.service.game;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Game;
import com.example.demo.model.Purchase;
import com.example.demo.model.User;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.PurchaseRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.realtime.NotificationService;

@Service
public class StoreService {
    
    private final GameRepository gameRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final GameLibraryService gameLibraryService;
    private final NotificationService notificationService;
  
    public StoreService(GameRepository gameRepository,
                       PurchaseRepository purchaseRepository,
                       UserRepository userRepository,
                       GameLibraryService gameLibraryService,
                       NotificationService notificationService) {
        this.gameRepository = gameRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.gameLibraryService = gameLibraryService;
        this.notificationService = notificationService;
    }
    
    public Page<Game> browseStore(Pageable pageable) {
        return gameRepository.findAll(pageable);
    }
    
    public List<Map<String, Object>> getFeaturedGames() {
        List<Game> featured = gameRepository.findByFeaturedTrue();
        
        return featured.stream().map(game -> {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id", game.getId());
            gameData.put("title", game.getTitle());
            gameData.put("coverImageUrl", game.getCoverImageUrl());
            gameData.put("price", game.getPrice());
            gameData.put("onSale", game.isOnSale());
            
            if (game.isOnSale()) {
                gameData.put("salePrice", game.getSalePrice());
                double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
                gameData.put("discountPercentage", Math.round(discount));
            }
            
            return gameData;
        }).collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getOnSaleGames() {
        List<Game> onSale = gameRepository.findByOnSaleTrue();
        
        return onSale.stream().map(game -> {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id", game.getId());
            gameData.put("title", game.getTitle());
            gameData.put("coverImageUrl", game.getCoverImageUrl());
            gameData.put("price", game.getPrice());
            gameData.put("salePrice", game.getSalePrice());
            
            double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
            gameData.put("discountPercentage", Math.round(discount));
            
            return gameData;
        }).collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getNewReleases() {
        List<Game> newReleases = gameRepository.findNewReleases(org.springframework.data.domain.PageRequest.of(0, 10));
        
        return newReleases.stream().map(game -> {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id", game.getId());
            gameData.put("title", game.getTitle());
            gameData.put("coverImageUrl", game.getCoverImageUrl());
            gameData.put("price", game.getPrice());
            gameData.put("releaseDate", game.getReleaseDate());
            gameData.put("onSale", game.isOnSale());
            
            if (game.isOnSale()) {
                gameData.put("salePrice", game.getSalePrice());
                double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
                gameData.put("discountPercentage", Math.round(discount));
            }
            
            return gameData;
        }).collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getTopRated() {
        List<Game> topRated = gameRepository.findTopRated(org.springframework.data.domain.PageRequest.of(0, 10));
        
        return topRated.stream().map(game -> {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id", game.getId());
            gameData.put("title", game.getTitle());
            gameData.put("coverImageUrl", game.getCoverImageUrl());
            gameData.put("price", game.getPrice());
            gameData.put("rating", game.getAverageRating());
            gameData.put("reviewCount", game.getReviewCount());
            gameData.put("onSale", game.isOnSale());
            
            if (game.isOnSale()) {
                gameData.put("salePrice", game.getSalePrice());
                double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
                gameData.put("discountPercentage", Math.round(discount));
            }
            
            return gameData;
        }).collect(Collectors.toList());
    }
    
    public Map<String, Object> getGameDetails(Long gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("id", game.getId());
        gameData.put("title", game.getTitle());
        gameData.put("description", game.getDescription());
        gameData.put("developer", game.getDeveloper());
        gameData.put("publisher", game.getPublisher());
        gameData.put("releaseDate", game.getReleaseDate());
        gameData.put("price", game.getPrice());
        gameData.put("onSale", game.isOnSale());
        gameData.put("genres", game.getGenres());
        gameData.put("tags", game.getTags());
        gameData.put("coverImageUrl", game.getCoverImageUrl());
        gameData.put("headerImageUrl", game.getHeaderImageUrl());
        gameData.put("screenshots", game.getScreenshots());
        gameData.put("averageRating", game.getAverageRating());
        gameData.put("reviewCount", game.getReviewCount());
        
        if (game.isOnSale()) {
            gameData.put("salePrice", game.getSalePrice());
            double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
            gameData.put("discountPercentage", Math.round(discount));
        }
        
        // Check if user owns the game
        if (userId != null) {
            boolean owned = gameLibraryService.hasGame(userId, gameId);
            gameData.put("owned", owned);
        }
        
        return gameData;
    }
    
    @Transactional
    public Purchase purchaseGame(Long userId, Long gameId, String paymentMethod) {
        // Check if user already has the game
        if (gameLibraryService.hasGame(userId, gameId)) {
            throw new IllegalStateException("Game already in user's library");
        }
        
        // Get user and game
        @SuppressWarnings("unused")
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Calculate price
        double price = game.isOnSale() ? game.getSalePrice() : game.getPrice();
        
        // Create purchase record
        Purchase purchase = new Purchase();
        purchase.setUserId(userId);
        purchase.setGameId(gameId);
        purchase.setPrice(price);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setPaymentMethod(paymentMethod);
        purchase.setTransactionId(UUID.randomUUID().toString());
        purchase.setStatus(Purchase.PurchaseStatus.COMPLETED);
        
        Purchase savedPurchase = purchaseRepository.save(purchase);
        
        // Add game to user's library
        gameLibraryService.addGameToLibrary(userId, gameId);
        
        // Send notification
        notificationService.sendNotification(
            userId,
            "Purchase Complete",
            "Thank you for purchasing " + game.getTitle(),
            "purchase_complete",
            Map.of(
                "gameId", gameId,
                "purchaseId", savedPurchase.getId()
            )
        );
        
        return savedPurchase;
    }
    
    public List<Map<String, Object>> getPurchaseHistory(Long userId) {
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        
        // Get all game IDs from purchases
        List<Long> gameIds = purchases.stream()
            .map(Purchase::getGameId)
            .collect(Collectors.toList());
        
        // Get all games in purchase history
        Map<Long, Game> gameMap = gameRepository.findAllById(gameIds).stream()
            .collect(Collectors.toMap(Game::getId, game -> game));
        
        // Build response with game details and purchase metadata
        return purchases.stream().map(purchase -> {
            Game game = gameMap.get(purchase.getGameId());
            if (game == null) return null;
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", purchase.getId());
            entry.put("gameId", game.getId());
            entry.put("title", game.getTitle());
            entry.put("coverImageUrl", game.getCoverImageUrl());
            entry.put("price", purchase.getPrice());
            entry.put("purchaseDate", purchase.getPurchaseDate());
            entry.put("status", purchase.getStatus().name());
            
            return entry;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    public double getTotalSpent(Long userId) {
        Double total = purchaseRepository.getTotalSpentByUser(userId);
        return total != null ? total : 0.0;
    }
    
    public Map<String, Object> getStoreStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalGames", gameRepository.count());
        stats.put("gamesOnSale", gameRepository.findByOnSaleTrue().size());
        
        // Get some featured game categories
        stats.put("featured", getFeaturedGames().size());
        stats.put("newReleases", getNewReleases().size());
        
        return stats;
    }
}