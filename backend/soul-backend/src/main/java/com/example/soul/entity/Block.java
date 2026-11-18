package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 黑名单实体（持久化到 MySQL blocks 表）。blocker = 拉黑者，blocked = 被拉黑者（有方向性）。
 */
@Entity
@Table(name = "blocks", uniqueConstraints = @UniqueConstraint(columnNames = {"blocker", "blocked"}))
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String blocker;

    @Column(nullable = false, length = 64)
    public String blocked;
}
