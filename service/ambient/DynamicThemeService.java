package com.example.demo.service.ambient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Game;
import com.example.demo.model.Profile;
import com.example.demo.repository.GameRepository;
import com.example.demo.repository.ProfileRepository;

@Service
public class DynamicThemeService {
    
    private final ProfileRepository profileRepository;
    private final GameRepository gameRepository;
    private final AmbientModeService ambientModeService;
    

    public DynamicThemeService(ProfileRepository profileRepository,
                              GameRepository gameRepository,
                              AmbientModeService ambientModeService) {
        this.profileRepository = profileRepository;
        this.gameRepository = gameRepository;
        this.ambientModeService = ambientModeService;
    }
    
    public Map<String, Object> getThemeForGame(Long gameId, Long userId) {
        // Get user's base theme color preference
        Optional<Profile> profileOpt = profileRepository.findByUserId(userId);
        String baseThemeColor = profileOpt.isPresent() ? 
            profileOpt.get().getThemeColor() : "#66c0f4";
        
        // Get game details to determine theme colors
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Determine primary genre color
        String primaryGenre = null;
        if (game.getGenres() != null && !game.getGenres().isEmpty()) {
            primaryGenre = game.getGenres().get(0).toLowerCase();
        }
        
        // Get color for genre
        Map<String, String> genreColors = ambientModeService.getAllGenreColors();
        String genreColor = genreColors.getOrDefault(primaryGenre, "#66c0f4");
        
        // Generate color palette from game and user preferences
        Map<String, Object> theme = generateColorPalette(baseThemeColor, genreColor);
        
        // Add additional theme elements
        theme.put("gameId", gameId);
        theme.put("gameTitle", game.getTitle());
        if (primaryGenre != null) {
            theme.put("genre", primaryGenre);
        }
        
        // Add image resources
        theme.put("headerImage", game.getHeaderImageUrl());
        theme.put("backgroundImage", game.getCoverImageUrl());
        
        // Add theme metadata
        theme.put("isDark", true); // Assume Steam always uses dark themes
        theme.put("contrastRatio", 4.5); // WCAG AA standard minimum
        
        return theme;
    }
    
    public Map<String, Object> getThemeForGenre(String genre, Long userId) {
        // Get user's base theme color preference
        Optional<Profile> profileOpt = profileRepository.findByUserId(userId);
        String baseThemeColor = profileOpt.isPresent() ? 
            profileOpt.get().getThemeColor() : "#66c0f4";
        
        // Normalize genre
        String normalizedGenre = genre.toLowerCase();
        
        // Get color for genre
        Map<String, String> genreColors = ambientModeService.getAllGenreColors();
        String genreColor = genreColors.getOrDefault(normalizedGenre, "#66c0f4");
        
        // Generate color palette
        Map<String, Object> theme = generateColorPalette(baseThemeColor, genreColor);
        
        // Add additional theme elements
        theme.put("genre", normalizedGenre);
        theme.put("isDark", true); // Assume Steam always uses dark themes
        
        return theme;
    }
    
    private Map<String, Object> generateColorPalette(String baseColor, String accentColor) {
        Map<String, Object> palette = new HashMap<>();
        
        // Create main colors
        palette.put("primary", accentColor);
        palette.put("secondary", baseColor);
        
        // Create background colors
        palette.put("background", "#1b2838"); // Steam dark blue
        palette.put("backgroundLight", "#2a3f5a");
        palette.put("backgroundDark", "#171a21");
        
        // Create text colors
        palette.put("textPrimary", "#ffffff");
        palette.put("textSecondary", "#c7d5e0");
        palette.put("textMuted", "#66c0f4");
        
        // Create UI colors
        palette.put("success", "#a4d007");
        palette.put("danger", "#d94126");
        palette.put("warning", "#f0ad4e");
        palette.put("info", "#66c0f4");
        
        // Create accent variants
        palette.put("primaryLight", lightenColor(accentColor, 0.3));
        palette.put("primaryDark", darkenColor(accentColor, 0.3));
        
        return palette;
    }
    
    public void saveUserThemePreference(Long userId, String themeColor) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        
        profile.setThemeColor(themeColor);
        profileRepository.save(profile);
    }
    
    private String lightenColor(String hexColor, double factor) {
        // Simple implementation that lightens a hex color
        // In a real implementation, you would use a color library
        return adjustColorBrightness(hexColor, factor, true);
    }
    
    private String darkenColor(String hexColor, double factor) {
        // Simple implementation that darkens a hex color
        return adjustColorBrightness(hexColor, factor, false);
    }
    
    @SuppressWarnings("UseSpecificCatch")
    private String adjustColorBrightness(String hexColor, double factor, boolean lighten) {
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
            return hexColor;
        }
        
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            
            if (lighten) {
                r = (int) Math.min(255, r + (255 - r) * factor);
                g = (int) Math.min(255, g + (255 - g) * factor);
                b = (int) Math.min(255, b + (255 - b) * factor);
            } else {
                r = (int) Math.max(0, r * (1 - factor));
                g = (int) Math.max(0, g * (1 - factor));
                b = (int) Math.max(0, b * (1 - factor));
            }
            
            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hexColor;
        }
    }
}