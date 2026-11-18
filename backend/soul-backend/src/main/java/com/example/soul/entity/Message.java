package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 聊天消息实体（持久化到 MySQL messages 表）。
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String senderUsername;

    @Column(nullable = false, length = 64)
    public String receiverUsername;

    @Column(length = 4000)
    public String content;

    @Column(length = 16)
    public String type;

    @Column(length = 512)
    public String mediaUrl;

    @Column(length = 64)
    public String createdAt;

    /** 是否已撤回（双方均不可见原文） */
    @Column(nullable = false)
    public boolean recalled = false;
}
