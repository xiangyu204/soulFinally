package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 公告板评论
 */
@Entity
@Table(name = "board_comments")
public class BoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long postId;

    @Column(length = 64)
    public String username;

    @Column(length = 64)
    public String author;

    @Column(length = 2000)
    public String content;

    @Column(length = 64)
    public String date;
}
