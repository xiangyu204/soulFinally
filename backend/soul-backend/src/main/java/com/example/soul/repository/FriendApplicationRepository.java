package com.example.soul.repository;

import com.example.soul.entity.FriendApplication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendApplicationRepository extends JpaRepository<FriendApplication, Long> {
    List<FriendApplication> findByToUserAndStatus(String toUser, String status);

    boolean existsByFromUserAndToUserAndStatus(String fromUser, String toUser, String status);
}
