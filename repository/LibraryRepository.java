package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Library;

@Repository
public interface LibraryRepository extends JpaRepository<Library, Long> {
    
    List<Library> findByUserId(Long userId);
    
    List<Library> findByUserIdOrderByLastPlayedDesc(Long userId);
    
    Optional<Library> findByUserIdAndGameId(Long userId, Long gameId);
    
    boolean existsByUserIdAndGameId(Long userId, Long gameId);
    
    @Query("SELECT l FROM Library l WHERE l.user.id = :userId AND l.favorite = true")
    List<Library> findFavoritesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT l FROM Library l WHERE l.user.id = :userId AND l.installed = true")
    List<Library> findInstalledByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(l) FROM Library l WHERE l.user.id = :userId")
    int countGamesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(l.playtime) FROM Library l WHERE l.user.id = :userId")
    Integer getTotalPlaytimeByUserId(@Param("userId") Long userId);
}