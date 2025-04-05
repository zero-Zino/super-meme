package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    
    List<Purchase> findByUserId(Long userId);
    
    Page<Purchase> findByUserIdOrderByPurchaseDateDesc(Long userId, Pageable pageable);
    
    List<Purchase> findByUserIdAndStatus(Long userId, Purchase.PurchaseStatus status);
    
    @Query("SELECT p FROM Purchase p WHERE p.userId = :userId AND p.purchaseDate >= :startDate AND p.purchaseDate <= :endDate")
    List<Purchase> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.price) FROM Purchase p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    Double getTotalSpentByUser(@Param("userId") Long userId);
}