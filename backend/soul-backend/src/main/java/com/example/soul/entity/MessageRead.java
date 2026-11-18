package com.example.soul.entity;

import jakarta.persistence.*;

/**
 * 用户对某会话的「已读」进度：记录该用户在该对话中已读到的最后一条消息 id。
 * 未读 = 对方发来的消息中 id 大于 lastReadMessageId 的数量。
 */
@Entity
@Table(name = "message_reads")
public class MessageRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /** 读者用户名 */
    @Column(nullable = false, length = 64)
    public String username;

    /** 对话对方用户名 */
    @Column(nullable = false, length = 64)
    public String partner;

    /** 已读到的最后一条消息 id（该对话内） */
    @Column(nullable = false)
    public Long lastReadMessageId;
}
