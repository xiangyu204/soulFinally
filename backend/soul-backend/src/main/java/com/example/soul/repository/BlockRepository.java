package com.example.soul.repository;

import com.example.soul.entity.Block;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {
    boolean existsByBlockerAndBlocked(String blocker, String blocked);

    void deleteByBlockerAndBlocked(String blocker, String blocked);

    List<Block> findByBlocker(String blocker);
}
