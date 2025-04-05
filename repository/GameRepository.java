package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    @SuppressWarnings("null")
    @Override
    Page<Game> findAll(Pageable pageable);
    
    Page<Game> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    @Query("SELECT g FROM Game g JOIN g.genres genre WHERE genre = :genre")
    Page<Game> findByGenre(@Param("genre") String genre, Pageable pageable);
    
    @Query("SELECT g FROM Game g JOIN g.tags tag WHERE tag = :tag")
    Page<Game> findByTag(@Param("tag") String tag, Pageable pageable);
    
    List<Game> findByFeaturedTrue();
    
    List<Game> findByOnSaleTrue();
    
    @Query("SELECT g FROM Game g ORDER BY g.releaseDate DESC")
    List<Game> findNewReleases(Pageable pageable);
    
    @Query("SELECT g FROM Game g ORDER BY g.averageRating DESC")
    List<Game> findTopRated(Pageable pageable);
    
    @Query("SELECT g FROM Game g JOIN g.genres genre WHERE genre = :genre ORDER BY g.averageRating DESC")
    List<Game> findTopRatedByGenre(@Param("genre") String genre, Pageable pageable);
    
    @Query("SELECT g FROM Game g WHERE g.price <= :maxPrice")
    Page<Game> findByMaxPrice(@Param("maxPrice") double maxPrice, Pageable pageable);
    
    @Query("SELECT g FROM Game g JOIN g.genres genre WHERE genre = :genre AND g.price <= :maxPrice")
    Page<Game> findByGenreAndMaxPrice(@Param("genre") String genre, @Param("maxPrice") double maxPrice, Pageable pageable);
}