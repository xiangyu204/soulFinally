package com.example.soul.repository;

import com.example.soul.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByName(String name);

    boolean existsByUsername(String username);

    /**
     * 妯＄硦鎼滅储锛氬悕瀛?/ 鐢ㄦ埛鍚?/ 鎯冲鎶€鑳?/ 鎯虫暀鎶€鑳?浠讳竴鍖呭惈鍏抽敭璇嶅嵆鍙紙涓嶅尯鍒嗗ぇ灏忓啓锛夈€?     * 渚嬶細鐢ㄦ埛 juy锛屾悳 j / ju / juy 閮借兘鍛戒腑銆?     */
    @Query("SELECT u FROM User u WHERE u.username <> :me AND (" +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(COALESCE(u.skillOffer, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(COALESCE(u.skillWant, '')) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "ORDER BY u.username ASC")
    List<User> searchUsers(@Param("me") String me, @Param("q") String q);
}
