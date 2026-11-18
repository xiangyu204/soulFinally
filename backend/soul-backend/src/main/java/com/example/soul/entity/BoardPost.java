package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 公告板帖子（Web 前端 Board 页使用）
 */
@Entity
@Table(name = "board_posts")
public class BoardPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(length = 64)
    public String username;

    @Column(length = 64)
    public String name;

    @Column(length = 512)
    public String avatar;

    @Column(length = 512)
    public String title;

    @Column(length = 4000)
    public String content;

    @Column(length = 64)
    public String category;

    @Column(length = 64)
    public String date;

    public Integer likes = 0;

    public Integer commentCount = 0;
}
