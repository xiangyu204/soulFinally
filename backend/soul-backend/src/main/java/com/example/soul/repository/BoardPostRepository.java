package com.example.soul.repository;

import com.example.soul.entity.BoardPost;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
}
