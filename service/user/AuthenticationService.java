package com.example.demo.service.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class AuthenticationService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Object user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        // Create Spring Security UserDetails from our User entity
        return new org.springframework.security.core.userdetails.User(
            ((User) user).getUsername(),
            ((User) user).getPassword(),
            ((User) user).isActive(),
            true,
            true,
            true,
            new ArrayList<>()
        );
    }
    
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Verify password matches and user is active
        boolean isAuthenticated = passwordEncoder.matches(password, user.getPassword()) && user.isActive();
        
        if (isAuthenticated) {
            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
        
        return isAuthenticated;
    }
    
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        Object user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, ((UserDetails) user).getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password (example: length check)
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        
        // Update password
        updatePassword((User) user, newPassword);
    }
    
    public void resetPassword(String email, String newPassword) {
        Object user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        
        // Validate new password (example: length check)
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        
        // Update password
        updatePassword((User) user, newPassword);
    }
    
    private void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}