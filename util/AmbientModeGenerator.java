package com.example.demo.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AmbientModeGenerator {
    
    // Generate particle system properties based on genre
    public Map<String, Object> generateParticleSystem(String genre, int intensity) {
        Map<String, Object> particleSystem = new HashMap<>();
        
        // Base properties
        particleSystem.put("enabled", true);
        particleSystem.put("intensity", intensity);
        
        // Adjust properties based on genre
        switch (genre.toLowerCase()) {
            case "action":
                particleSystem.put("particleCount", 80 * (intensity / 50.0));
                particleSystem.put("particleSize", 3);
                particleSystem.put("particleSpeed", 3 * (intensity / 50.0));
                particleSystem.put("turbulence", 2.0);
                particleSystem.put("particleColor", "#e74c3c");
                particleSystem.put("pattern", "burst");
                break;
                
            case "adventure":
                particleSystem.put("particleCount", 60 * (intensity / 50.0));
                particleSystem.put("particleSize", 4);
                particleSystem.put("particleSpeed", 1.5 * (intensity / 50.0));
                particleSystem.put("turbulence", 1.0);
                particleSystem.put("particleColor", "#f39c12");
                particleSystem.put("pattern", "flowing");
                break;
                
            case "rpg":
                particleSystem.put("particleCount", 100 * (intensity / 50.0));
                particleSystem.put("particleSize", 2.5);
                particleSystem.put("particleSpeed", 1.0 * (intensity / 50.0));
                particleSystem.put("turbulence", 0.5);
                particleSystem.put("particleColor", "#9b59b6");
                particleSystem.put("pattern", "magical");
                break;
                
            case "strategy":
                particleSystem.put("particleCount", 120 * (intensity / 50.0));
                particleSystem.put("particleSize", 2);
                particleSystem.put("particleSpeed", 0.8 * (intensity / 50.0));
                particleSystem.put("turbulence", 0.2);
                particleSystem.put("particleColor", "#2ecc71");
                particleSystem.put("pattern", "grid");
                break;
                
            case "simulation":
                particleSystem.put("particleCount", 150 * (intensity / 50.0));
                particleSystem.put("particleSize", 1.5);
                particleSystem.put("particleSpeed", 0.5 * (intensity / 50.0));
                particleSystem.put("turbulence", 0.3);
                particleSystem.put("particleColor", "#3498db");
                particleSystem.put("pattern", "system");
                break;
                
            case "horror":
                particleSystem.put("particleCount", 40 * (intensity / 50.0));
                particleSystem.put("particleSize", 5);
                particleSystem.put("particleSpeed", 0.7 * (intensity / 50.0));
                particleSystem.put("turbulence", 1.5);
                particleSystem.put("particleColor", "#34495e");
                particleSystem.put("pattern", "creepy");
                break;
                
            default:
                // Default settings
                particleSystem.put("particleCount", 70 * (intensity / 50.0));
                particleSystem.put("particleSize", 3);
                particleSystem.put("particleSpeed", 1.0 * (intensity / 50.0));
                particleSystem.put("turbulence", 0.8);
                particleSystem.put("particleColor", "#66c0f4");
                particleSystem.put("pattern", "random");
                break;
        }
        
        return particleSystem;
    }
    
    // Generate background color gradient
    public Map<String, Object> generateBackgroundGradient(String genre, int colorIntensity) {
        Map<String, Object> gradient = new HashMap<>();
        
        // Base properties
        gradient.put("enabled", true);
        gradient.put("intensity", colorIntensity);
        
        // Set colors based on genre
        switch (genre.toLowerCase()) {
            case "action":
                gradient.put("primaryColor", "#e74c3c");
                gradient.put("secondaryColor", "#c0392b");
                break;
                
            case "adventure":
                gradient.put("primaryColor", "#f39c12");
                gradient.put("secondaryColor", "#d35400");
                break;
                
            case "rpg":
                gradient.put("primaryColor", "#9b59b6");
                gradient.put("secondaryColor", "#8e44ad");
                break;
                
            case "strategy":
                gradient.put("primaryColor", "#2ecc71");
                gradient.put("secondaryColor", "#27ae60");
                break;
                
            case "simulation":
                gradient.put("primaryColor", "#3498db");
                gradient.put("secondaryColor", "#2980b9");
                break;
                
            case "horror":
                gradient.put("primaryColor", "#34495e");
                gradient.put("secondaryColor", "#2c3e50");
                break;
                
            default:
                gradient.put("primaryColor", "#66c0f4");
                gradient.put("secondaryColor", "#171a21");
                break;
        }
        
        // Gradient type
        gradient.put("type", "radial");
        
        // Calculate alpha based on intensity (0-100)
        double alpha = colorIntensity / 100.0 * 0.7; // Max alpha of 0.7
        gradient.put("alpha", alpha);
        
        return gradient;
    }
    
    // Generate soundscape settings
    public Map<String, Object> generateSoundscape(String genre, boolean enabled, int volume) {
        Map<String, Object> soundscape = new HashMap<>();
        
        // Base properties
        soundscape.put("enabled", enabled);
        soundscape.put("volume", volume / 100.0); // Convert 0-100 to 0-1
        
        // Set audio tracks based on genre
        switch (genre.toLowerCase()) {
            case "action":
                soundscape.put("ambientTrack", "soundscapes/action_ambient.mp3");
                soundscape.put("effects", Arrays.asList(
                    "soundscapes/effects/explosion_distant.mp3",
                    "soundscapes/effects/gunfire_distant.mp3"
                ));
                soundscape.put("tempo", "fast");
                break;
                
            case "adventure":
                soundscape.put("ambientTrack", "soundscapes/adventure_ambient.mp3");
                soundscape.put("effects", Arrays.asList(
                    "soundscapes/effects/wind_howling.mp3",
                    "soundscapes/effects/birds_distant.mp3"
                ));
                soundscape.put("tempo", "medium");
                break;
                
            case "rpg":
                soundscape.put("ambientTrack", "soundscapes/rpg_ambient.mp3");
                soundscape.put("effects", Arrays.asList(
                    "soundscapes/effects/magic_chime.mp3",
                    "soundscapes/effects/tavern_background.mp3"
                ));
                soundscape.put("tempo", "slow");
                break;
                
            case "strategy":
                soundscape.put("ambientTrack", "soundscapes/strategy_ambient.mp3");
                soundscape.put("effects", Arrays.asList(
                    "soundscapes/effects/paper_shuffle.mp3",
                    "soundscapes/effects/chess_piece.mp3"
                ));
                soundscape.put("tempo", "slow");
                break;
                
            case "horror":
                soundscape.put("ambientTrack", "soundscapes/horror_ambient.mp3");
                soundscape.put("effects", Arrays.asList(
                    "soundscapes/effects/creaky_door.mp3",
                    "soundscapes/effects/heart_beat.mp3"
                ));
                soundscape.put("tempo", "very_slow");
                break;
                
            default:
                soundscape.put("ambientTrack", "soundscapes/default_ambient.mp3");
                soundscape.put("effects", Arrays.asList(
                    "soundscapes/effects/notification.mp3",
                    "soundscapes/effects/interface_click.mp3"
                ));
                soundscape.put("tempo", "medium");
                break;
        }
        
        return soundscape;
    }
}