package com.example.soul.store;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存临时状态（通话信令、AI 测验会话）。
 * 用户 / 消息 / 匹配关系已持久化到 MySQL。
 */
@Component
public class Store {

    public static class Question {
        public Long questionId;
        public String question;
        public String type; // CHOICE / FILL_BLANK
        public List<String> options;
        public String answer;
    }

    public static class Quiz {
        public Long quizId;
        public String username;
        public String skill;
        public String quizType;
        public List<Question> questions;
    }

    public static class Call {
        public String callId;
        public String caller;
        public String callee;
        public String channelName;
        public String callType; // VOICE / VIDEO
        public String status;   // RINGING / ACTIVE / ENDED / REJECTED
        public long updatedAt;
        public long answeredAt; // 0=未接听；>0=接听时间戳（用于通话时长）
    }

    public static class RoomMessage {
        public String sender;
        public String content;
        public long timestamp;
    }

    public static class Room {
        public String roomId;
        public String roomName;
        public String owner;
        public List<String> participants = new ArrayList<>();
        public Map<String, String> invitations = new HashMap<>(); // username -> PENDING/ACCEPTED/REJECTED
        public List<RoomMessage> messages = new ArrayList<>();
        public long createdAt;
    }

    public final List<Quiz> quizzes = new ArrayList<>();
    public final Map<String, Call> calls = new HashMap<>();
    public final Map<String, Room> rooms = new HashMap<>();
    public final AtomicLong idGen = new AtomicLong(1);

    public long nextId() {
        return idGen.getAndIncrement();
    }
}
