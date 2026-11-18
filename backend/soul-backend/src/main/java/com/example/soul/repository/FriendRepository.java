package com.example.soul.repository;

import com.example.soul.entity.Friend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    boolean existsByUserAAndUserB(String userA, String userB);

    void deleteByUserAAndUserB(String userA, String userB);

    List<Friend> findByUserAOrUserB(String userA, String userB);
}
