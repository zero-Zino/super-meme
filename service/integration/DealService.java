package com.example.demo.service.integration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.model.Game;
import com.example.demo.repository.GameRepository;

@Service
public class DealService {
    
    private final GameRepository gameRepository;
    
    // Cache of current featured deals
    private List<Map<String, Object>> featuredDeals = new ArrayList<>();
    private LocalDateTime lastFeaturedDealsUpdate = LocalDateTime.now();
 
    public DealService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    
    public List<Map<String, Object>> getFeaturedDeals() {
        // Check if we need to refresh the featured deals
        if (featuredDeals.isEmpty() || 
            LocalDateTime.now().isAfter(lastFeaturedDealsUpdate.plusHours(6))) {
            updateFeaturedDeals();
        }
        
        return featuredDeals;
    }
    
    @Scheduled(cron = "0 0 */6 * * ?") // Run every 6 hours
    public void updateFeaturedDeals() {
        // Get games that are on sale
        List<Game> onSaleGames = gameRepository.findByOnSaleTrue();
        
        // Sort by discount percentage (highest first)
        List<Game> sortedGames = onSaleGames.stream()
            .sorted((g1, g2) -> {
                double discount1 = (1 - (g1.getSalePrice() / g1.getPrice())) * 100;
                double discount2 = (1 - (g2.getSalePrice() / g2.getPrice())) * 100;
                return Double.compare(discount2, discount1);
            })
            .limit(10) // Top 10 deals
            .collect(Collectors.toList());
        
        // Format for response
        featuredDeals = sortedGames.stream()
            .map(game -> {
                Map<String, Object> dealData = new HashMap<>();
                dealData.put("id", game.getId());
                dealData.put("title", game.getTitle());
                dealData.put("coverUrl", game.getCoverImageUrl());
                dealData.put("originalPrice", game.getPrice());
                dealData.put("salePrice", game.getSalePrice());
                
                double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
                dealData.put("discountPercentage", Math.round(discount));
                
                return dealData;
            })
            .collect(Collectors.toList());
        
        lastFeaturedDealsUpdate = LocalDateTime.now();
    }
    
    public List<Map<String, Object>> getDealsByGenre(String genre) {
        // Get games on sale with the specified genre
        List<Game> genreGames = gameRepository.findByGenre(genre, null).getContent();
        List<Game> onSaleGenreGames = genreGames.stream()
            .filter(Game::isOnSale)
            .collect(Collectors.toList());
        
        // Sort by discount percentage (highest first)
        List<Game> sortedGames = onSaleGenreGames.stream()
            .sorted((g1, g2) -> {
                double discount1 = (1 - (g1.getSalePrice() / g1.getPrice())) * 100;
                double discount2 = (1 - (g2.getSalePrice() / g2.getPrice())) * 100;
                return Double.compare(discount2, discount1);
            })
            .limit(10) // Top 10 deals in this genre
            .collect(Collectors.toList());
        
        // Format for response
        return sortedGames.stream()
            .map(game -> {
                Map<String, Object> dealData = new HashMap<>();
                dealData.put("id", game.getId());
                dealData.put("title", game.getTitle());
                dealData.put("coverUrl", game.getCoverImageUrl());
                dealData.put("originalPrice", game.getPrice());
                dealData.put("salePrice", game.getSalePrice());
                
                double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
                dealData.put("discountPercentage", Math.round(discount));
                
                return dealData;
            })
            .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getDealsForUserWishlist(Long userId, List<Long> wishlistGameIds) {
        // Get games on sale from user's wishlist
        List<Game> wishlistGames = gameRepository.findAllById(wishlistGameIds);
        List<Game> onSaleWishlistGames = wishlistGames.stream()
            .filter(Game::isOnSale)
            .collect(Collectors.toList());
        
        // Sort by discount percentage (highest first)
        List<Game> sortedGames = onSaleWishlistGames.stream()
            .sorted((g1, g2) -> {
                double discount1 = (1 - (g1.getSalePrice() / g1.getPrice())) * 100;
                double discount2 = (1 - (g2.getSalePrice() / g2.getPrice())) * 100;
                return Double.compare(discount2, discount1);
            })
            .collect(Collectors.toList());
        
        // Format for response
        return sortedGames.stream()
            .map(game -> {
                Map<String, Object> dealData = new HashMap<>();
                dealData.put("id", game.getId());
                dealData.put("title", game.getTitle());
                dealData.put("coverUrl", game.getCoverImageUrl());
                dealData.put("originalPrice", game.getPrice());
                dealData.put("salePrice", game.getSalePrice());
                
                double discount = (1 - (game.getSalePrice() / game.getPrice())) * 100;
                dealData.put("discountPercentage", Math.round(discount));
                
                return dealData;
            })
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> createWeeklySale() {
        // In a real implementation, this would create a new weekly sale
        // For now, we'll return simulated data
        
        Map<String, Object> saleData = new HashMap<>();
        saleData.put("name", "Weekly Special");
        saleData.put("startDate", LocalDateTime.now());
        saleData.put("endDate", LocalDateTime.now().plusDays(7));
        saleData.put("featuredGames", getFeaturedDeals());
        
        return saleData;
    }
}