package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ambient_settings")
public class AmbientSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;
    
    private boolean enabled = true;
    
    private int particleIntensity = 50; // 0-100 scale
    
    private int colorIntensity = 70; // 0-100 scale
    
    private boolean soundEnabled = true;
    
    private int soundVolume = 30; // 0-100 scale
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getParticleIntensity() {
        return particleIntensity;
    }

    public void setParticleIntensity(int particleIntensity) {
        this.particleIntensity = particleIntensity;
    }

    public int getColorIntensity() {
        return colorIntensity;
    }

    public void setColorIntensity(int colorIntensity) {
        this.colorIntensity = colorIntensity;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public int getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(int soundVolume) {
        this.soundVolume = soundVolume;
    }
}