package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 好友备注实体（持久化到 MySQL remarks 表）。
 * 备注是「自己可见」的：owner 给 target 设置的备注，只有 owner 自己能看到。
 */
@Entity
@Table(name = "remarks", uniqueConstraints = @UniqueConstraint(columnNames = {"owner", "target"}))
public class Remark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 64)
    public String owner;

    @Column(nullable = false, length = 64)
    public String target;

    @Column(length = 64)
    public String remark;
}
