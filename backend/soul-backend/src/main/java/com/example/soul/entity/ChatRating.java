package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 聊天评分（Web 前端 Chat 页使用）
 */
@Entity
@Table(name = "chat_ratings")
public class ChatRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long roomId;

    @Column(length = 64, nullable = false)
    public String username;

    public Integer score;
}
