package com.example.demo.service.ambient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.AmbientSettings;
import com.example.demo.model.Game;
import com.example.demo.repository.AmbientSettingsRepository;
import com.example.demo.repository.GameRepository;

@Service
public class AmbientModeService {
    
    private final AmbientSettingsRepository ambientSettingsRepository;
    private final GameRepository gameRepository;
    
    // Maps game genres to color schemes
    private final Map<String, String> genreColorMap = new HashMap<>();
    
    // Maps game genres to particle behaviors
    private final Map<String, Map<String, Object>> genreParticleMap = new HashMap<>();
    

    public AmbientModeService(AmbientSettingsRepository ambientSettingsRepository,
                             GameRepository gameRepository) {
        this.ambientSettingsRepository = ambientSettingsRepository;
        this.gameRepository = gameRepository;
        
        // Initialize genre to color mapping
        initializeGenreMappings();
    }
    
    private void initializeGenreMappings() {
        // Color mappings
        genreColorMap.put("action", "#e74c3c");
        genreColorMap.put("adventure", "#f39c12");
        genreColorMap.put("rpg", "#9b59b6"); 
        genreColorMap.put("strategy", "#2ecc71");
        genreColorMap.put("simulation", "#3498db");
        genreColorMap.put("sports", "#1abc9c");
        genreColorMap.put("racing", "#e67e22");
        genreColorMap.put("indie", "#95a5a6");
        genreColorMap.put("horror", "#34495e");
        genreColorMap.put("puzzle", "#1abc9c");
        genreColorMap.put("shooter", "#e74c3c");
        genreColorMap.put("platformer", "#3498db");
        
        // Particle behaviors
        genreParticleMap.put("action", Map.of(
            "speed", 3.0,
            "count", 80,
            "size", 3.0,
            "turbulence", 2.0,
            "pattern", "burst"
        ));
        
        genreParticleMap.put("adventure", Map.of(
            "speed", 1.5,
            "count", 60,
            "size", 4.0,
            "turbulence", 1.0,
            "pattern", "flowing"
        ));
        
        genreParticleMap.put("rpg", Map.of(
            "speed", 1.0,
            "count", 100,
            "size", 2.5,
            "turbulence", 0.5,
            "pattern", "magical"
        ));
        
        genreParticleMap.put("strategy", Map.of(
            "speed", 0.8,
            "count", 120,
            "size", 2.0,
            "turbulence", 0.2,
            "pattern", "grid"
        ));
        
        genreParticleMap.put("simulation", Map.of(
            "speed", 0.5,
            "count", 150,
            "size", 1.5,
            "turbulence", 0.3,
            "pattern", "system"
        ));
        
        genreParticleMap.put("horror", Map.of(
            "speed", 0.7,
            "count", 40,
            "size", 5.0,
            "turbulence", 1.5,
            "pattern", "creepy"
        ));
        
        // Default for other genres
        genreParticleMap.put("default", Map.of(
            "speed", 1.0,
            "count", 70,
            "size", 3.0,
            "turbulence", 0.8,
            "pattern", "random"
        ));
    }
    
    public AmbientSettings getUserSettings(Long userId) {
        return ambientSettingsRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Create default settings if not found
                AmbientSettings defaultSettings = new AmbientSettings();
                defaultSettings.setUserId(userId);
                defaultSettings.setEnabled(true);
                defaultSettings.setParticleIntensity(50);
                defaultSettings.setColorIntensity(70);
                defaultSettings.setSoundEnabled(true);
                defaultSettings.setSoundVolume(30);
                return ambientSettingsRepository.save(defaultSettings);
            });
    }
    
    public AmbientSettings updateUserSettings(AmbientSettings settings) {
        // Check if settings already exist
        Optional<AmbientSettings> existingSettings = ambientSettingsRepository.findByUserId(settings.getUserId());
        
        if (existingSettings.isPresent()) {
            AmbientSettings existing = existingSettings.get();
            existing.setEnabled(settings.isEnabled());
            existing.setParticleIntensity(settings.getParticleIntensity());
            existing.setColorIntensity(settings.getColorIntensity());
            existing.setSoundEnabled(settings.isSoundEnabled());
            existing.setSoundVolume(settings.getSoundVolume());
            
            return ambientSettingsRepository.save(existing);
        } else {
            return ambientSettingsRepository.save(settings);
        }
    }
    
    public Map<String, Object> generateAmbientEffects(Long gameId, AmbientSettings settings) {
        Map<String, Object> effects = new HashMap<>();
        
        if (!settings.isEnabled()) {
            effects.put("enabled", false);
            return effects;
        }
        
        // Get game details to determine ambiance
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Get primary genre
        String primaryGenre = "default";
        if (game.getGenres() != null && !game.getGenres().isEmpty()) {
            primaryGenre = game.getGenres().get(0).toLowerCase();
        }
        
        // Get base color for genre
        String baseColor = genreColorMap.getOrDefault(primaryGenre, "#66c0f4");
        
        // Get particle behavior for genre
        Map<String, Object> particleBehavior = genreParticleMap.getOrDefault(
            primaryGenre, genreParticleMap.get("default"));
        
        // Calculate intensity based on user settings
        double colorIntensity = settings.getColorIntensity() / 100.0;
        double particleMultiplier = settings.getParticleIntensity() / 50.0;
        
        // Generate effects
        effects.put("enabled", true);
        effects.put("baseColor", baseColor);
        effects.put("colorIntensity", colorIntensity);
        
        // Generate particle settings
        Map<String, Object> particles = new HashMap<>();
        particles.put("count", (int)(((Number)particleBehavior.get("count")).doubleValue() * particleMultiplier));
        particles.put("speed", ((Number)particleBehavior.get("speed")).doubleValue() * particleMultiplier);
        particles.put("size", ((Number)particleBehavior.get("size")).doubleValue());
        particles.put("turbulence", ((Number)particleBehavior.get("turbulence")).doubleValue());
        particles.put("pattern", particleBehavior.get("pattern"));
        
        effects.put("particles", particles);
        effects.put("soundEnabled", settings.isSoundEnabled());
        effects.put("soundVolume", settings.getSoundVolume());
        
        // Generate metadata about the game for immersion
        effects.put("gameTitle", game.getTitle());
        effects.put("gameCover", game.getCoverImageUrl());
        effects.put("gameGenre", primaryGenre);
        
        return effects;
    }
    
    public Map<String, Object> generateAmbientEffectsForGenre(String gameGenre, AmbientSettings settings) {
        Map<String, Object> effects = new HashMap<>();
        
        if (!settings.isEnabled()) {
            effects.put("enabled", false);
            return effects;
        }
        
        // Normalize genre
        String genre = gameGenre.toLowerCase();
        
        // Get base color for genre
        String baseColor = genreColorMap.getOrDefault(genre, "#66c0f4");
        
        // Get particle behavior for genre
        Map<String, Object> particleBehavior = genreParticleMap.getOrDefault(
            genre, genreParticleMap.get("default"));
        
        // Calculate intensity based on user settings
        double colorIntensity = settings.getColorIntensity() / 100.0;
        double particleMultiplier = settings.getParticleIntensity() / 50.0;
        
        // Generate particle settings
        int particleCount = (int)(((Number)particleBehavior.get("count")).doubleValue() * particleMultiplier);
        double particleSpeed = ((Number)particleBehavior.get("speed")).doubleValue() * particleMultiplier;
        double particleSize = ((Number)particleBehavior.get("size")).doubleValue();
        
        effects.put("enabled", true);
        effects.put("baseColor", baseColor);
        effects.put("colorIntensity", colorIntensity);
        effects.put("particleCount", particleCount);
        effects.put("particleSpeed", particleSpeed);
        effects.put("particleSize", particleSize);
        effects.put("particleTurbulence", particleBehavior.get("turbulence"));
        effects.put("particlePattern", particleBehavior.get("pattern"));
        effects.put("soundEnabled", settings.isSoundEnabled());
        effects.put("soundVolume", settings.getSoundVolume());
        
        return effects;
    }
    
    public void toggleAmbientMode(Long userId, boolean enabled) {
        AmbientSettings settings = getUserSettings(userId);
        settings.setEnabled(enabled);
        ambientSettingsRepository.save(settings);
    }
    
    public Map<String, String> getAllGenreColors() {
        return new HashMap<>(genreColorMap);
    }
    
    public Map<String, Object> getGenreParticlePresets(String genre) {
        return genreParticleMap.getOrDefault(genre.toLowerCase(), genreParticleMap.get("default"));
    }
}