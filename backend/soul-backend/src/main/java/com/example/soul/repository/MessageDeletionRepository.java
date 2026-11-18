package com.example.soul.repository;

import com.example.soul.entity.MessageDeletion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageDeletionRepository extends JpaRepository<MessageDeletion, Long> {

    boolean existsByMessageIdAndUsername(Long messageId, String username);

    List<MessageDeletion> findByUsername(String username);
}
