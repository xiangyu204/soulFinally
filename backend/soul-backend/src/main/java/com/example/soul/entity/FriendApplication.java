package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 好友申请实体（持久化到 MySQL friend_requests 表）。
 * 发申请 -> 对方在申请列表同意后，才成为好友。
 */
@Entity
@Table(name = "friend_requests")
public class FriendApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String fromUser;

    @Column(nullable = false, length = 64)
    public String toUser;

    @Column(length = 256)
    public String message;

    @Column(length = 16)
    public String status; // PENDING / ACCEPTED / REJECTED

    @Column(length = 64)
    public String createdAt;
}
