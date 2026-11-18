package com.example.soul.repository;

import com.example.soul.entity.BoardComment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    List<BoardComment> findByPostIdOrderByIdAsc(Long postId);

    long countByPostId(Long postId);
}
