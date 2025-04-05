package com.example.demo.service.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Profile;
import com.example.demo.repository.ProfileRepository;

@Service
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private final String uploadDir = "uploads/profiles/";
    
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(uploadDir + "avatars"));
            Files.createDirectories(Paths.get(uploadDir + "backgrounds"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }
    
    public Profile getProfile(Long userId) {
        return profileRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Create default profile
                Profile profile = new Profile();
                profile.setUserId(userId);
                profile.setDisplayName("User" + userId);
                profile.setBio("No bio yet.");
                profile.setAvatarUrl("default_avatar.png");
                profile.setProfileBackgroundUrl("default_background.jpg");
                profile.setThemeColor("#66c0f4");
                profile.setLastUpdated(LocalDateTime.now());
                return profileRepository.save(profile);
            });
    }
    
    public Profile updateProfile(Profile profile) {
        // Validate profile belongs to user
        Profile existingProfile = profileRepository.findByUserId(profile.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user"));
        
        // Update fields
        existingProfile.setDisplayName(profile.getDisplayName());
        existingProfile.setBio(profile.getBio());
        existingProfile.setThemeColor(profile.getThemeColor());
        existingProfile.setCustomStatus(profile.getCustomStatus());
        existingProfile.setCountryCode(profile.getCountryCode());
        existingProfile.setLanguage(profile.getLanguage());
        existingProfile.setLastUpdated(LocalDateTime.now());
        
        // Privacy settings
        existingProfile.setShowPlaytime(profile.isShowPlaytime());
        existingProfile.setShowLibrary(profile.isShowLibrary());
        existingProfile.setShowWishlist(profile.isShowWishlist());
        existingProfile.setShowFriends(profile.isShowFriends());
        existingProfile.setShowAchievements(profile.isShowAchievements());
        
        return profileRepository.save(existingProfile);
    }
    
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        Profile profile = getProfile(userId);
        
        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(uploadDir + "avatars/" + filename);
        
        // Copy file to target location
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Update profile with new avatar URL
        profile.setAvatarUrl(filename);
        profile.setLastUpdated(LocalDateTime.now());
        profileRepository.save(profile);
        
        return filename;
    }
    
    public String uploadBackground(Long userId, MultipartFile file) throws IOException {
        Profile profile = getProfile(userId);
        
        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(uploadDir + "backgrounds/" + filename);
        
        // Copy file to target location
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Update profile with new background URL
        profile.setProfileBackgroundUrl(filename);
        profile.setLastUpdated(LocalDateTime.now());
        profileRepository.save(profile);
        
        return filename;
    }
    
    public void updateTheme(Long userId, String themeColor) {
        Profile profile = getProfile(userId);
        profile.setThemeColor(themeColor);
        profile.setLastUpdated(LocalDateTime.now());
        profileRepository.save(profile);
    }
    
    public void updatePrivacySettings(Long userId, Map<String, Boolean> privacySettings) {
        Profile profile = getProfile(userId);
        
        if (privacySettings.containsKey("showPlaytime")) {
            profile.setShowPlaytime(privacySettings.get("showPlaytime"));
        }
        if (privacySettings.containsKey("showLibrary")) {
            profile.setShowLibrary(privacySettings.get("showLibrary"));
        }
        if (privacySettings.containsKey("showWishlist")) {
            profile.setShowWishlist(privacySettings.get("showWishlist"));
        }
        if (privacySettings.containsKey("showFriends")) {
            profile.setShowFriends(privacySettings.get("showFriends"));
        }
        if (privacySettings.containsKey("showAchievements")) {
            profile.setShowAchievements(privacySettings.get("showAchievements"));
        }
        
        profile.setLastUpdated(LocalDateTime.now());
        profileRepository.save(profile);
    }
    
    public void updateCustomStatus(Long userId, String status) {
        Profile profile = getProfile(userId);
        profile.setCustomStatus(status);
        profile.setLastUpdated(LocalDateTime.now());
        profileRepository.save(profile);
    }
    
    public Map<String, Object> getProfileStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // This would typically aggregate data from various services
        // For now, we'll use placeholder logic
        
        // TODO: Replace with actual service calls to get real stats
        stats.put("totalGames", 0);
        stats.put("totalPlaytime", 0);
        stats.put("achievementCount", 0);
        stats.put("reviewCount", 0);
        stats.put("memberSince", "");
        
        return stats;
    }
}