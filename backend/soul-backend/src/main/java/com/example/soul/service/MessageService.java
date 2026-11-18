package com.example.soul.service;

import com.example.soul.entity.Message;
import com.example.soul.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** 易덃겘?쇿뀯?곮??뽨툗映삣엹鰲꾥똽?뽧?*/
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message appendMessage(String sender, String receiver, String content) {
        return appendMessage(sender, receiver, content, "TEXT", "");
    }

    public Message appendMessage(String sender, String receiver, String content, String type, String mediaUrl) {
        Message m = new Message();
        m.senderUsername = sender;
        m.receiverUsername = receiver;
        m.content = content;
        m.type = normalizeMessageType(type);
        m.mediaUrl = mediaUrl == null ? "" : mediaUrl;
        m.createdAt = Instant.now().toString();
        return messageRepository.save(m);
    }

    public void appendCallMessage(String caller, String callee, String content, String callType) {
        Message m = new Message();
        m.senderUsername = caller;
        m.receiverUsername = callee;
        m.content = content;
        m.type = "CALL";
        m.mediaUrl = (callType == null ? "VOICE" : callType);
        m.createdAt = Instant.now().toString();
        messageRepository.save(m);
    }

    public String lastMessage(String a, String b) {
        Message m = lastMessageObj(a, b);
        return m == null ? null : m.content;
    }

    public Message lastMessageObj(String a, String b) {
        List<Message> list = messageRepository.findBetweenDesc(a, b);
        return list.isEmpty() ? null : list.get(0);
    }
    public String normalizeMessageType(String type) {
        if ("IMAGE".equalsIgnoreCase(type)) return "IMAGE";
        if ("CALL".equalsIgnoreCase(type)) return "CALL";
        return "TEXT";
    }

    public long parseInstantMillis(String iso) {
        try {
            return Instant.parse(iso).toEpochMilli();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }
}
