package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 好友关系实体（持久化到 MySQL friends 表）。userA < userB（字典序归一化）。
 */
@Entity
@Table(name = "friends", uniqueConstraints = @UniqueConstraint(columnNames = {"userA", "userB"}))
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String userA;

    @Column(nullable = false, length = 64)
    public String userB;
}
