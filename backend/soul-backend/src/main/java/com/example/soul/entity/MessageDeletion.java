package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 消息「仅自己删除」记录：某用户对某条消息执行了删除（只在自己这边消失，对方仍可见）。
 */
@Entity
@Table(name = "message_deletions")
public class MessageDeletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long messageId;

    @Column(nullable = false, length = 64)
    public String username;
}
