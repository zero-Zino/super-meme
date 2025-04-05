package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Friend;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    
    List<Friend> findByUserId(Long userId);
    
    List<Friend> findByUserIdAndStatus(Long userId, Friend.FriendStatus status);
    
    List<Friend> findByFriendIdAndStatus(Long friendId, Friend.FriendStatus status);
    
    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
    Optional<Friend> findFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    @Query("SELECT COUNT(f) FROM Friend f WHERE f.userId = :userId AND f.status = 'ACCEPTED'")
    int countAcceptedFriendsByUserId(@Param("userId") Long userId);
}