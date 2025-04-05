package com.example.demo.service.ambient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.model.AmbientSettings;
import com.example.demo.model.Game;
import com.example.demo.repository.AmbientSettingsRepository;
import com.example.demo.repository.GameRepository;

@Service
public class SoundscapeService {
    
    private final AmbientSettingsRepository ambientSettingsRepository;
    private final GameRepository gameRepository;
    
    // Maps game genres to soundscape types
    private final Map<String, Map<String, Object>> genreSoundscapeMap = new HashMap<>();
    

    public SoundscapeService(AmbientSettingsRepository ambientSettingsRepository,
                            GameRepository gameRepository) {
        this.ambientSettingsRepository = ambientSettingsRepository;
        this.gameRepository = gameRepository;
        
        // Initialize genre to soundscape mapping
        initializeGenreSoundscapes();
    }
    
    private void initializeGenreSoundscapes() {
        // Action games
        genreSoundscapeMap.put("action", Map.of(
            "ambientTrack", "soundscapes/action_ambient.mp3",
            "effects", List.of(
                "soundscapes/effects/explosion_distant.mp3",
                "soundscapes/effects/gunfire_distant.mp3"
            ),
            "tempo", "fast",
            "volume", 0.7,
            "description", "High-energy, intense soundscape with distant action sounds"
        ));
        
        // Adventure games
        genreSoundscapeMap.put("adventure", Map.of(
            "ambientTrack", "soundscapes/adventure_ambient.mp3",
            "effects", List.of(
                "soundscapes/effects/wind_howling.mp3",
                "soundscapes/effects/birds_distant.mp3"
            ),
            "tempo", "medium",
            "volume", 0.6,
            "description", "Atmospheric, exploration-focused soundscape with natural elements"
        ));
        
        // RPG games
        genreSoundscapeMap.put("rpg", Map.of(
            "ambientTrack", "soundscapes/rpg_ambient.mp3",
            "effects", List.of(
                "soundscapes/effects/magic_chime.mp3",
                "soundscapes/effects/tavern_background.mp3"
            ),
            "tempo", "slow",
            "volume", 0.5,
            "description", "Fantasy-inspired, melodic soundscape with magical elements"
        ));
        
        // Strategy games
        genreSoundscapeMap.put("strategy", Map.of(
            "ambientTrack", "soundscapes/strategy_ambient.mp3",
            "effects", List.of(
                "soundscapes/effects/paper_shuffle.mp3",
                "soundscapes/effects/chess_piece.mp3"
            ),
            "tempo", "slow",
            "volume", 0.4,
            "description", "Thoughtful, focused soundscape with subtle tactical elements"
        ));
        
        // Horror games
        genreSoundscapeMap.put("horror", Map.of(
            "ambientTrack", "soundscapes/horror_ambient.mp3",
            "effects", List.of(
                "soundscapes/effects/creaky_door.mp3",
                "soundscapes/effects/heart_beat.mp3"
            ),
            "tempo", "very_slow",
            "volume", 0.6,
            "description", "Eerie, tension-building soundscape with unsettling effects"
        ));
        
        // Default soundscape
        genreSoundscapeMap.put("default", Map.of(
            "ambientTrack", "soundscapes/default_ambient.mp3",
            "effects", List.of(
                "soundscapes/effects/notification.mp3",
                "soundscapes/effects/interface_click.mp3"
            ),
            "tempo", "medium",
            "volume", 0.5,
            "description", "Neutral, balanced soundscape suitable for browsing"
        ));
    }
    
    public Map<String, Object> getSoundscapeForGame(Long gameId, AmbientSettings settings) {
        if (!settings.isEnabled() || !settings.isSoundEnabled()) {
            return Map.of("enabled", false);
        }
        
        // Get game details to determine soundscape
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        
        // Get primary genre
        String primaryGenre = "default";
        if (game.getGenres() != null && !game.getGenres().isEmpty()) {
            primaryGenre = game.getGenres().get(0).toLowerCase();
        }
        
        // Get soundscape for genre
        Map<String, Object> soundscape = genreSoundscapeMap.getOrDefault(
            primaryGenre, genreSoundscapeMap.get("default"));
        
        // Apply user volume setting
        double volumeMultiplier = settings.getSoundVolume() / 100.0;
        double baseVolume = ((Number) soundscape.get("volume")).doubleValue();
        double adjustedVolume = baseVolume * volumeMultiplier;
        
        // Create result with user-adjusted settings
        Map<String, Object> result = new HashMap<>(soundscape);
        result.put("enabled", true);
        result.put("volume", adjustedVolume);
        result.put("genre", primaryGenre);
        result.put("gameTitle", game.getTitle());
        
        return result;
    }
    
    public Map<String, Object> getSoundscapeForGenre(String genre, AmbientSettings settings) {
        if (!settings.isEnabled() || !settings.isSoundEnabled()) {
            return Map.of("enabled", false);
        }
        
        // Normalize genre
        String normalizedGenre = genre.toLowerCase();
        
        // Get soundscape for genre
        Map<String, Object> soundscape = genreSoundscapeMap.getOrDefault(
            normalizedGenre, genreSoundscapeMap.get("default"));
        
        // Apply user volume setting
        double volumeMultiplier = settings.getSoundVolume() / 100.0;
        double baseVolume = ((Number) soundscape.get("volume")).doubleValue();
        double adjustedVolume = baseVolume * volumeMultiplier;
        
        // Create result with user-adjusted settings
        Map<String, Object> result = new HashMap<>(soundscape);
        result.put("enabled", true);
        result.put("volume", adjustedVolume);
        result.put("genre", normalizedGenre);
        
        return result;
    }
    
    public List<Map<String, Object>> getAllSoundscapes() {
        List<Map<String, Object>> soundscapes = new ArrayList<>();
        
        genreSoundscapeMap.forEach((genre, soundscape) -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("genre", genre);
            entry.put("description", soundscape.get("description"));
            entry.put("ambientTrack", soundscape.get("ambientTrack"));
            entry.put("tempo", soundscape.get("tempo"));
            
            soundscapes.add(entry);
        });
        
        return soundscapes;
    }
    
    public void toggleSoundscape(Long userId, boolean enabled) {
        AmbientSettings settings = ambientSettingsRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Create default settings if not found
                AmbientSettings defaultSettings = new AmbientSettings();
                defaultSettings.setUserId(userId);
                defaultSettings.setEnabled(true);
                defaultSettings.setSoundEnabled(true);
                defaultSettings.setSoundVolume(30);
                return defaultSettings;
            });
        
        settings.setSoundEnabled(enabled);
        ambientSettingsRepository.save(settings);
    }
    
    public void adjustSoundscapeVolume(Long userId, int volume) {
        AmbientSettings settings = ambientSettingsRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Create default settings if not found
                AmbientSettings defaultSettings = new AmbientSettings();
                defaultSettings.setUserId(userId);
                defaultSettings.setEnabled(true);
                defaultSettings.setSoundEnabled(true);
                return defaultSettings;
            });
        
        // Ensure volume is between 0 and 100
        int adjustedVolume = Math.max(0, Math.min(100, volume));
        
        settings.setSoundVolume(adjustedVolume);
        ambientSettingsRepository.save(settings);
    }
}