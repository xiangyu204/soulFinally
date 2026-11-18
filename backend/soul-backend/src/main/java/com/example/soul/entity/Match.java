package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 匹配关系实体（持久化到 MySQL user_matches 表）。userA < userB（按字典序归一化）。
 */
@Entity
@Table(name = "user_matches", uniqueConstraints = @UniqueConstraint(columnNames = {"userA", "userB"}))
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String userA;

    @Column(nullable = false, length = 64)
    public String userB;
}
