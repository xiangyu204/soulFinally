package com.example.soul.repository;

import com.example.soul.entity.ChatRating;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRatingRepository extends JpaRepository<ChatRating, Long> {

    Optional<ChatRating> findByRoomIdAndUsername(Long roomId, String username);
}
