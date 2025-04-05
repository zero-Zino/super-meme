package com.example.demo.service.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.model.Game;
import com.example.demo.repository.GameRepository;

@Service
public class PriceService {
    
    private final GameRepository gameRepository;
    private final SteamAPIService steamAPIService;
    
    @Autowired
    public PriceService(GameRepository gameRepository, SteamAPIService steamAPIService) {
        this.gameRepository = gameRepository;
        this.steamAPIService = steamAPIService;
    }
    
    public Map<String, Object> getGamePrice(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        Map<String, Object> priceData = new HashMap<>();
        priceData.put("gameId", game.getId());
        priceData.put("title", game.getTitle());
        priceData.put("price", game.getPrice());
        priceData.put("onSale", game.isOnSale());
        
        if (game.isOnSale()) {
            priceData.put("salePrice", game.getSalePrice());
            double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
            priceData.put("discountPercentage", Math.round(discount));
        }
        
        return priceData;
    }
    
    public List<Map<String, Object>> getOnSaleGames() {
        List<Game> onSaleGames = gameRepository.findByOnSaleTrue();
        
        return onSaleGames.stream().map(game -> {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("id", game.getId());
            gameData.put("title", game.getTitle());
            gameData.put("coverUrl", game.getCoverImageUrl());
            gameData.put("price", game.getPrice());
            gameData.put("salePrice", game.getSalePrice());
            
            double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
            gameData.put("discountPercentage", Math.round(discount));
            
            return gameData;
        }).collect(Collectors.toList());
    }
    
    @Scheduled(cron = "0 0 */6 * * ?") // Run every 6 hours
    public void updatePrices() {
        // In a real implementation, this would call Steam API
        // For now, we'll simulate updating prices for some games
        
        List<Game> allGames = gameRepository.findAll();
        Random random = new Random();
        
        // Update about 10% of games
        int updateCount = Math.max(1, allGames.size() / 10);
        
        for (int i = 0; i < updateCount; i++) {
            int index = random.nextInt(allGames.size());
            Game game = allGames.get(index);
            
            // Randomly put games on sale or update prices
            boolean putOnSale = random.nextBoolean();
            
            if (putOnSale && !game.isOnSale()) {
                // Put game on sale
                double discount = (random.nextInt(7) + 1) * 10; // 10% to 70% discount
                double salePrice = game.getPrice() * (1 - (discount / 100));
                salePrice = Math.round(salePrice * 100) / 100.0; // Round to 2 decimal places
                
                game.setSalePrice(salePrice);
                game.setOnSale(true);
            } else if (game.isOnSale()) {
                // End sale
                game.setOnSale(false);
            }
            
            gameRepository.save(game);
        }
    }
    
    public void updateGamePrice(Long gameId, double newPrice) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        game.setPrice(newPrice);
        
        // If on sale, adjust sale price
        if (game.isOnSale()) {
            // Maintain same discount percentage
            double currentDiscount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
            double newSalePrice = newPrice * (1 - (currentDiscount / 100));
            newSalePrice = Math.round(newSalePrice * 100) / 100.0; // Round to 2 decimal places
            
            game.setSalePrice(newSalePrice);
        }
        
        gameRepository.save(game);
    }
    
    public void setGameOnSale(Long gameId, double salePrice, boolean onSale) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        game.setSalePrice(salePrice);
        game.setOnSale(onSale);
        
        gameRepository.save(game);
    }
    
    public Map<String, Object> getPriceHistory(Long gameId) {
        // In a real implementation, this would fetch price history
        // For now, we'll return simulated data
        
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        Map<String, Object> priceHistory = new HashMap<>();
        priceHistory.put("gameId", game.getId());
        priceHistory.put("title", game.getTitle());
        
        // Simulate 6 months of price history
        List<Map<String, Object>> historyPoints = new ArrayList<>();
        Random random = new Random(game.getId().intValue()); // Seed with game ID for consistency
        
        double basePrice = game.getPrice();
        
        for (int i = 0; i < 180; i++) {
            LocalDateTime date = LocalDateTime.now().minusDays(180 - i);
            
            // Create random sales events
            boolean onSale = i % 30 == 0 || (i > 150 && i % 15 == 0);
            double price = onSale ? 
                basePrice * (1 - ((random.nextInt(7) + 1) * 0.1)) : basePrice;
            
            Map<String, Object> point = new HashMap<>();
            point.put("date", date.toString());
            point.put("price", Math.round(price * 100) / 100.0);
            point.put("onSale", onSale);
            
            historyPoints.add(point);
        }
        
        priceHistory.put("history", historyPoints);
        
        // Add lowest and highest prices
        Map<String, Object> lowestPrice = historyPoints.stream()
            .min(Comparator.comparing(p -> ((Double) p.get("price"))))
            .orElse(new HashMap<>());
        
        Map<String, Object> highestPrice = historyPoints.stream()
            .max(Comparator.comparing(p -> ((Double) p.get("price"))))
            .orElse(new HashMap<>());
        
        priceHistory.put("lowestPrice", lowestPrice);
        priceHistory.put("highestPrice", highestPrice);
        
        return priceHistory;
    }
}