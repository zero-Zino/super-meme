package com.example.demo.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.model.Game;

@Component
public class GameDataParser {
    
    // Parse game data from CSV file
    public List<Game> parseGameDataFromCsv(String csvData) {
        List<Game> games = new ArrayList<>();
        
        String[] lines = csvData.split("\n");
        // Skip header
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] fields = line.split(",");
            
            if (fields.length < 10) continue; // Skip invalid lines
            
            try {
                Game game = new Game();
                
                // Parse fields
                game.setTitle(fields[1].trim());
                
                if (fields[2] != null && !fields[2].isEmpty()) {
                    game.setDeveloper(fields[2].trim());
                }
                
                if (fields[3] != null && !fields[3].isEmpty()) {
                    game.setPublisher(fields[3].trim());
                }
                
                // Parse release date
                try {
                    String dateString = fields[4].trim();
                    if (dateString.length() >= 4) {
                        int year = Integer.parseInt(dateString.substring(0, 4));
                        game.setReleaseDate(LocalDate.of(year, 1, 1));
                    }
                } catch (Exception e) {
                    // Default to current year if date parsing fails
                    game.setReleaseDate(LocalDate.now());
                }
                
                // Parse price
                try {
                    double price = Double.parseDouble(fields[5].trim());
                    game.setPrice(price);
                } catch (Exception e) {
                    game.setPrice(29.99); // Default price
                }
                
                // Parse genres
                if (fields[6] != null && !fields[6].isEmpty()) {
                    String[] genres = fields[6].split(";");
                    game.setGenres(Arrays.asList(genres));
                } else {
                    game.setGenres(Collections.singletonList("Uncategorized"));
                }
                
                // Set other fields with default values
                game.setCoverImageUrl("default_cover.jpg");
                game.setHeaderImageUrl("default_header.jpg");
                game.setScreenshots(new ArrayList<>());
                game.setTags(new ArrayList<>());
                game.setAverageRating(0.0);
                game.setReviewCount(0);
                game.setFeatured(false);
                game.setOnSale(false);
                
                games.add(game);
            } catch (Exception e) {
                // Skip this game if parsing fails
                System.err.println("Error parsing game data: " + e.getMessage());
            }
        }
        
        return games;
    }
    
    // Parse game data from JSON
    public List<Game> parseGameDataFromJson(String jsonData) {
        List<Game> games = new ArrayList<>();
        
        // In a real implementation, use a JSON library like Jackson
        // For simplicity, we'll return an empty list
        
        return games;
    }
    
    // Generate system requirements string
    public String generateSystemRequirements(Game game) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("MINIMUM:\n");
        sb.append("OS: Windows 10\n");
        sb.append("Processor: Intel Core i5-3570K / AMD FX-8310\n");
        sb.append("Memory: 8 GB RAM\n");
        sb.append("Graphics: NVIDIA GeForce GTX 780 / AMD Radeon RX 470\n");
        sb.append("DirectX: Version 12\n");
        sb.append("Storage: 70 GB available space\n\n");
        
        sb.append("RECOMMENDED:\n");
        sb.append("OS: Windows 10\n");
        sb.append("Processor: Intel Core i7-4790 / AMD Ryzen 5 1500X\n");
        sb.append("Memory: 16 GB RAM\n");
        sb.append("Graphics: NVIDIA GeForce GTX 1060 / AMD Radeon RX 580\n");
        sb.append("DirectX: Version 12\n");
        sb.append("Storage: 70 GB available space\n");
        
        return sb.toString();
    }
}