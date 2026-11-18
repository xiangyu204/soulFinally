-- ============================================================
-- SOUL 技能交换平台 · 数据库结构文件
-- 导出时间：2026-11-05
-- 目标库：soul
-- DBMS：MariaDB 10.5.29（MySQL 兼容，InnoDB 引擎，utf8mb4_unicode_ci）
-- 共 12 张表；源库未声明物理外键（FK），关联关系见文件末尾「逻辑外键」
-- ============================================================

CREATE DATABASE IF NOT EXISTS `soul` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `soul`;

-- ------------------------------------------------------------
-- 1. 用户表
-- 技能（Skills）：无独立表，存于 skill_offer / skill_want 两个 CSV 字段
-- 可交换时段（Schedule）：存于 time_slot 字段（weekday_morning 等）
-- 评分汇总：average_rating / rating_count（明细见 chat_ratings）
-- ------------------------------------------------------------
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `skill_offer` varchar(512) DEFAULT NULL,
  `skill_want` varchar(512) DEFAULT NULL,
  `skill_offer_level` varchar(255) DEFAULT NULL,
  `skill_want_level` varchar(255) DEFAULT NULL,
  `nationality` varchar(255) DEFAULT NULL,
  `average_rating` double DEFAULT NULL,
  `rating_count` int(11) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `time_slot` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_users_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 2. 匹配关系表（匹配成功后双方即互为聊天对象，「聊天室」由此派生）
-- ------------------------------------------------------------
CREATE TABLE `user_matches` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `usera` varchar(64) NOT NULL,
  `userb` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_user_matches_pair` (`usera`,`userb`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 3. 好友关系表
-- ------------------------------------------------------------
CREATE TABLE `friends` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `usera` varchar(64) NOT NULL,
  `userb` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_friends_pair` (`usera`,`userb`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 4. 好友申请表
-- ------------------------------------------------------------
CREATE TABLE `friend_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_user` varchar(64) NOT NULL,
  `to_user` varchar(64) NOT NULL,
  `message` varchar(256) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `created_at` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 5. 聊天消息表（一对一消息；「聊天室」= 双方 username 组合）
-- ------------------------------------------------------------
CREATE TABLE `messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sender_username` varchar(64) NOT NULL,
  `receiver_username` varchar(64) NOT NULL,
  `content` varchar(4000) DEFAULT NULL,
  `type` varchar(16) DEFAULT NULL,
  `media_url` varchar(512) DEFAULT NULL,
  `recalled` bit(1) NOT NULL,
  `created_at` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 6. 消息已读表（记录每个用户对某个对话的最后已读消息）
-- ------------------------------------------------------------
CREATE TABLE `message_reads` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `partner` varchar(64) NOT NULL,
  `last_read_message_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 7. 消息删除表（按用户维度的「删除」，不影响对方）
-- ------------------------------------------------------------
CREATE TABLE `message_deletions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `message_id` bigint(20) NOT NULL,
  `username` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 8. 聊天评分表（评价 / Review 的明细记录）
-- 按「聊天室 room_id + 评分人 username」记录 1-5 分
-- 汇总值写入 users.average_rating / users.rating_count
-- ------------------------------------------------------------
CREATE TABLE `chat_ratings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL,
  `username` varchar(64) NOT NULL,
  `score` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 9. 拉黑表
-- ------------------------------------------------------------
CREATE TABLE `blocks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `blocker` varchar(64) NOT NULL,
  `blocked` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_blocks_pair` (`blocker`,`blocked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 10. 好友备注表
-- ------------------------------------------------------------
CREATE TABLE `remarks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `owner` varchar(64) NOT NULL,
  `target` varchar(64) NOT NULL,
  `remark` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_remarks_pair` (`owner`,`target`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 11. 告示板帖子表
-- ------------------------------------------------------------
CREATE TABLE `board_posts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL,
  `title` varchar(512) DEFAULT NULL,
  `content` varchar(4000) DEFAULT NULL,
  `category` varchar(64) DEFAULT NULL,
  `date` varchar(64) DEFAULT NULL,
  `likes` int(11) DEFAULT NULL,
  `comment_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 12. 告示板评论表
-- ------------------------------------------------------------
CREATE TABLE `board_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `post_id` bigint(20) NOT NULL,
  `username` varchar(64) DEFAULT NULL,
  `author` varchar(64) DEFAULT NULL,
  `content` varchar(2000) DEFAULT NULL,
  `date` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 逻辑外键关系（源库未建立物理 FK，以下为应用层实际关联）
-- ============================================================
-- users.username            ←  user_matches.usera / .userb
-- users.username            ←  friends.usera / .userb
-- users.username            ←  friend_requests.from_user / .to_user
-- users.username            ←  messages.sender_username / .receiver_username
-- users.username            ←  message_reads.username / .partner
-- users.username            ←  message_deletions.username
-- users.id                  ←  chat_ratings.room_id（room 由匹配双方派生）
-- users.username            ←  chat_ratings.username
-- users.username            ←  blocks.blocker / .blocked
-- users.username            ←  remarks.owner / .target
-- users.username            ←  board_posts.username
-- users.username            ←  board_comments.username
-- messages.id               ←  message_deletions.message_id
-- messages.id               ←  message_reads.last_read_message_id
-- board_posts.id            ←  board_comments.post_id
--
-- 如需补齐物理外键，可执行（需先清理脏数据）：
-- ALTER TABLE user_matches     ADD CONSTRAINT fk_um_a FOREIGN KEY (usera) REFERENCES users(username) ON DELETE CASCADE;
-- ALTER TABLE user_matches     ADD CONSTRAINT fk_um_b FOREIGN KEY (userb) REFERENCES users(username) ON DELETE CASCADE;
-- ALTER TABLE friends          ADD CONSTRAINT fk_fr_a FOREIGN KEY (usera) REFERENCES users(username) ON DELETE CASCADE;
-- ALTER TABLE friends          ADD CONSTRAINT fk_fr_b FOREIGN KEY (userb) REFERENCES users(username) ON DELETE CASCADE;
-- ALTER TABLE messages         ADD CONSTRAINT fk_msg_s FOREIGN KEY (sender_username) REFERENCES users(username) ON DELETE CASCADE;
-- ALTER TABLE messages         ADD CONSTRAINT fk_msg_r FOREIGN KEY (receiver_username) REFERENCES users(username) ON DELETE CASCADE;
-- ALTER TABLE board_comments   ADD CONSTRAINT fk_bc_post FOREIGN KEY (post_id) REFERENCES board_posts(id) ON DELETE CASCADE;
-- ALTER TABLE message_deletions ADD CONSTRAINT fk_del_msg FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE;
