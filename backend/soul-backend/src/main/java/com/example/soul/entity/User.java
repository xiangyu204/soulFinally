package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 用户实体（持久化到 MySQL users 表）。
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false, length = 64)
    public String username;

    public String password;
    public String name;
    public String email;

    @Column(length = 512)
    public String avatar;

    public String gender;
    public Integer age;

    @Column(length = 512)
    public String skillOffer;

    @Column(length = 512)
    public String skillWant;

    public String skillOfferLevel;
    public String skillWantLevel;
    public String nationality;

    public Double averageRating;
    public Integer ratingCount;
    public String role;
    public String phone;
    public String address;
    public String timeSlot;
}
