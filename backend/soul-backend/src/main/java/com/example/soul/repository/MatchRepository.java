package com.example.soul.repository;

import com.example.soul.entity.Match;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
    boolean existsByUserAAndUserB(String userA, String userB);
}
