package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "profiles")
public class Profile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private Long userId;
    
    private String displayName;
    
    @Column(length = 500)
    private String bio;
    
    private String avatarUrl;
    
    private String profileBackgroundUrl;
    
    private String themeColor;
    
    private LocalDateTime lastUpdated;
    
    // Privacy settings
    private boolean showPlaytime = true;
    
    private boolean showLibrary = true;
    
    private boolean showWishlist = true;
    
    private boolean showFriends = true;
    
    private boolean showAchievements = true;
    
    // Additional profile settings
    private String customStatus;
    
    private String countryCode;
    
    private String language;
    
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getProfileBackgroundUrl() {
        return profileBackgroundUrl;
    }

    public void setProfileBackgroundUrl(String profileBackgroundUrl) {
        this.profileBackgroundUrl = profileBackgroundUrl;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isShowPlaytime() {
        return showPlaytime;
    }

    public void setShowPlaytime(boolean showPlaytime) {
        this.showPlaytime = showPlaytime;
    }

    public boolean isShowLibrary() {
        return showLibrary;
    }

    public void setShowLibrary(boolean showLibrary) {
        this.showLibrary = showLibrary;
    }

    public boolean isShowWishlist() {
        return showWishlist;
    }

    public void setShowWishlist(boolean showWishlist) {
        this.showWishlist = showWishlist;
    }

    public boolean isShowFriends() {
        return showFriends;
    }

    public void setShowFriends(boolean showFriends) {
        this.showFriends = showFriends;
    }

    public boolean isShowAchievements() {
        return showAchievements;
    }

    public void setShowAchievements(boolean showAchievements) {
        this.showAchievements = showAchievements;
    }

    public String getCustomStatus() {
        return customStatus;
    }

    public void setCustomStatus(String customStatus) {
        this.customStatus = customStatus;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    @PreUpdate
    @PrePersist
    public void updateTimestamp() {
        lastUpdated = LocalDateTime.now();
    }
}