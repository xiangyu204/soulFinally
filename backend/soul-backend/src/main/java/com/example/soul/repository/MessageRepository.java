package com.example.soul.repository;

import com.example.soul.entity.Message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.senderUsername = :a AND m.receiverUsername = :b) OR (m.senderUsername = :b AND m.receiverUsername = :a) ORDER BY m.id ASC")
    List<Message> findBetween(@Param("a") String a, @Param("b") String b);

    @Query("SELECT m FROM Message m WHERE (m.senderUsername = :a AND m.receiverUsername = :b) OR (m.senderUsername = :b AND m.receiverUsername = :a) ORDER BY m.id DESC")
    List<Message> findBetweenDesc(@Param("a") String a, @Param("b") String b);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderUsername = :partner AND m.receiverUsername = :me AND m.id > :lastReadId")
    long countUnreadFrom(@Param("partner") String partner, @Param("me") String me, @Param("lastReadId") long lastReadId);
}
