package com.example.soul.repository;

import com.example.soul.entity.MessageRead;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {

    Optional<MessageRead> findByUsernameAndPartner(String username, String partner);
}
