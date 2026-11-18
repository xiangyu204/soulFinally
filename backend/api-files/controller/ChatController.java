package com.example.soul.controller;

import com.example.soul.service.MessageService;
import com.example.soul.service.RelationService;

import com.example.soul.entity.ChatRating;
import com.example.soul.entity.Friend;
import com.example.soul.entity.Message;
import com.example.soul.entity.MessageDeletion;
import com.example.soul.entity.MessageRead;
import com.example.soul.entity.User;
import com.example.soul.repository.ChatRatingRepository;
import com.example.soul.repository.MessageDeletionRepository;
import com.example.soul.repository.MessageReadRepository;
import com.example.soul.repository.MessageRepository;
import com.example.soul.repository.UserRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * 聊天相关接口控制器。
 * <p>
 * 负责：聊天室列表、创建匹配聊天、消息查询、发送文字/图片消息、
 * 消息撤回与删除（仅自己可见）、已读标记与未读数（红点）、聊天评分、聊天内 AI 帮手。
 * 请求/响应用到的 DTO（如 ChatSendRequest、ChatSendResponse、MessageActionRequest、
 * ReadRequest、BackendChatMessage）以及 userRepository、relationService 等依赖均继承自 BaseApiController。
 */
@RestController
public class ChatController extends BaseApiController {

    // ============ 聊天 ============

    /**
     * GET /api/chat/rooms —— 获取聊天室列表。
     *
     * @param username 当前用户名
     * @return 与当前用户已匹配的每个对象的信息：对方用户名、昵称、头像，以及最后一条消息的
     *         内容/类型/媒体地址/时间（已撤回的消息显示为“[消息已撤回]”；没有消息时为空字符串）
     */
    @GetMapping("/api/chat/rooms")
    public List<Map<String, Object>> getChatRooms(@RequestParam("username") String username) {
        List<Map<String, Object>> result = new ArrayList<>();
        // 遍历所有用户，跳过自己以及尚未匹配的用户
        for (User u : userRepository.findAll()) {
            if (u.username.equals(username)) continue;
            if (!relationService.isMatched(username, u.username)) continue;
            Message lastMsg = messageService.lastMessageObj(username, u.username);
            Map<String, Object> m = new HashMap<>();
            m.put("partnerUsername", u.username);
            m.put("partnerName", u.name);
            m.put("avatar", u.avatar);
            m.put("lastMessage", lastMsg == null ? "" : (lastMsg.recalled ? "[消息已撤回]" : lastMsg.content));
            m.put("lastMessageType", lastMsg == null ? "TEXT" : messageService.normalizeMessageType(lastMsg.type));
            m.put("lastMessageMedia", lastMsg == null || lastMsg.mediaUrl == null ? "" : lastMsg.mediaUrl);
            m.put("lastMessageTime", lastMsg == null ? "" : lastMsg.createdAt);
            result.add(m);
        }
        return result;
    }

    /**
     * POST /api/chat/direct —— 直接创建聊天室（建立匹配）。
     *
     * @param req 请求体：senderUsername 发送方、receiverUsername 接收方、
     *            content/type/mediaUrl 可选的首条消息
     * @return success=true 与提示“匹配成功”
     * 业务规则：先校验双方用户存在；若带有非空内容，则同时写入首条消息。
     */
    @PostMapping("/api/chat/direct")
    public Map<String, Object> createDirectChatRoom(@RequestBody ChatSendRequest req) {
        relationService.validateUsers(req.senderUsername(), req.receiverUsername());

        relationService.addMatch(req.senderUsername(), req.receiverUsername());
        if (req.content() != null && !req.content().isBlank()) {
            messageService.appendMessage(req.senderUsername(), req.receiverUsername(), req.content(), req.type(), req.mediaUrl());
        }
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        m.put("message", "匹配成功");
        return m;
    }

    /**
     * GET /api/chat/messages —— 获取两个用户之间的聊天记录。
     *
     * @param me      当前用户名
     * @param partner 聊天对象用户名
     * @return 消息列表；当前用户“仅自己删除”的消息会被过滤掉（对方仍可见）
     */
    @GetMapping("/api/chat/messages")
    public List<BackendChatMessage> getChatMessages(@RequestParam("me") String me,
                                                    @RequestParam("partner") String partner) {
        relationService.validateUsers(me, partner);
        // 收集当前用户已删除的消息 ID，后面用于过滤
        Set<Long> deletedIds = new HashSet<>();
        for (MessageDeletion d : messageDeletionRepository.findByUsername(me)) {
            deletedIds.add(d.messageId);
        }
        List<BackendChatMessage> result = new ArrayList<>();
        for (Message msg : messageRepository.findBetween(me, partner)) {
            if (deletedIds.contains(msg.id)) continue;
            result.add(toChatMessage(msg));
        }
        return result;
    }

    /**
     * POST /api/chat/send —— 发送文字/图片消息。
     *
     * @param req 请求体：senderUsername、receiverUsername、content、type（TEXT/IMAGE）、mediaUrl
     * @return ChatSendResponse（success、message、已保存的消息）
     * 业务规则：
     * 1. 任一方拉黑对方则 403；
     * 2. TEXT 内容不能为空，IMAGE 地址不能为空，内容不超过 4000 字符；
     * 3. 图片仅好友之间可发；
     * 4. 文字需为好友或已匹配；
     * 5. 非好友时，若自己已发出最后一条消息而对方未回复，则不能继续发送（只能发一条）。
     */
    @PostMapping("/api/chat/send")
    public ChatSendResponse sendMessage(@RequestBody ChatSendRequest req) {
        relationService.validateUsers(req.senderUsername(), req.receiverUsername());
        if (relationService.isBlockedEither(req.senderUsername(), req.receiverUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "对方已把你拉黑，消息发送失败");
        }
        String type = messageService.normalizeMessageType(req.type());
        String content = req.content() == null ? "" : req.content().trim();
        String mediaUrl = req.mediaUrl() == null ? "" : req.mediaUrl().trim();
        if ("TEXT".equals(type) && content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "消息不能为空");
        }
        if ("IMAGE".equals(type) && mediaUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片地址不能为空");
        }
        if (content.length() > 4000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "消息不能超过 4000 个字符");
        }
        // 查询双方关系：好友 / 已匹配
        boolean friend = relationService.isFriend(req.senderUsername(), req.receiverUsername());
        boolean matched = relationService.isMatched(req.senderUsername(), req.receiverUsername());
        if ("IMAGE".equals(type) && !friend) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "加为好友后才能发图片");
        }
        if ("TEXT".equals(type) && !friend && !matched) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "匹配后才能聊天");
        }
        // 非好友的文字消息：最后一条是自己发的（对方还没回复）则拒绝
        if ("TEXT".equals(type) && !friend) {
            Message last = messageService.lastMessageObj(req.senderUsername(), req.receiverUsername());
            if (last != null && last.senderUsername.equals(req.senderUsername())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "对方还没回复，暂时只能发一条消息");
            }
        }
        // 校验通过：确保匹配关系存在，再保存消息
        relationService.addMatch(req.senderUsername(), req.receiverUsername());
        Message saved = messageService.appendMessage(req.senderUsername(), req.receiverUsername(), content, type, mediaUrl);
        return new ChatSendResponse(true, "发送成功", toChatMessage(saved));
    }


    /**
     * POST /api/chat/image —— 上传并发送图片消息（multipart/form-data）。
     *
     * @param senderUsername   发送方用户名
     * @param receiverUsername 接收方用户名
     * @param file             图片文件
     * @return ChatSendResponse（success、message、已保存的消息，mediaUrl 为 /uploads/文件名）
     * 业务规则：不能被拉黑；必须是好友；文件非空、Content-Type 以 image/ 开头、大小不超过 10MB；
     * 文件以随机 UUID 命名保存到上传目录。
     */
    @PostMapping("/api/chat/image")
    public ChatSendResponse sendImage(@RequestParam("senderUsername") String senderUsername,
                                      @RequestParam("receiverUsername") String receiverUsername,
                                      @RequestParam("file") MultipartFile file) {
        relationService.validateUsers(senderUsername, receiverUsername);
        if (relationService.isBlockedEither(senderUsername, receiverUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "对方已把你拉黑，图片发送失败");
        }
        if (!relationService.isFriend(senderUsername, receiverUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "加为好友后才能发图片");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只支持图片文件");
        }
        if (file.getSize() > 10L * 1024L * 1024L) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "图片不能超过 10MB");
        }
        try {
            // 确保上传目录存在，不存在则创建
            File dir = new File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("无法创建上传目录");
            }
            // 取原文件扩展名；没有扩展名时默认 .jpg
            String original = file.getOriginalFilename();
            String suffix = original != null && original.lastIndexOf('.') >= 0
                    ? original.substring(original.lastIndexOf('.')) : ".jpg";
            String filename = UUID.randomUUID() + suffix;
            file.transferTo(new File(dir, filename).getAbsoluteFile());
            String mediaUrl = "/uploads/" + filename;
            relationService.addMatch(senderUsername, receiverUsername);
            Message saved = messageService.appendMessage(senderUsername, receiverUsername, "", "IMAGE", mediaUrl);
            return new ChatSendResponse(true, "图片发送成功", toChatMessage(saved));
        } catch (ResponseStatusException e) {
            // 业务异常原样抛出，其他异常统一包装为 500
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "图片上传失败", e);
        }
    }

    // ============ 消息撤回 / 删除（仅自己） ============

    /**
     * POST /api/chat/recall —— 撤回消息。
     *
     * @param req 请求体：username 操作者、messageId 消息 ID
     * @return success=true 与提示“已撤回”
     * 业务规则：消息必须存在；只能撤回自己发送的消息；发送超过 2 分钟不可撤回。
     */
    @PostMapping("/api/chat/recall")
    public Map<String, Object> recallMessage(@RequestBody MessageActionRequest req) {
        relationService.validateUsername(req.username());
        Message msg = messageRepository.findById(req.messageId()).orElse(null);
        if (msg == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "消息不存在");
        }
        if (!msg.senderUsername.equals(req.username())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能撤回自己发送的消息");
        }
        // 未撤回且距发送时间超过 2 分钟则不允许撤回
        if (!msg.recalled && System.currentTimeMillis() - messageService.parseInstantMillis(msg.createdAt) > 2 * 60 * 1000L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "超过2分钟，无法撤回");
        }
        msg.recalled = true;
        messageRepository.save(msg);
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        m.put("message", "已撤回");
        return m;
    }

    /**
     * POST /api/chat/delete —— 删除消息（仅对自己隐藏，不影响对方）。
     *
     * @param req 请求体：username 操作者、messageId 消息 ID
     * @return success=true 与提示“已删除”
     * 业务规则：消息必须存在；通过 MessageDeletion 记录实现，重复删除不会重复插入。
     */
    @PostMapping("/api/chat/delete")
    public Map<String, Object> deleteMessage(@RequestBody MessageActionRequest req) {
        relationService.validateUsername(req.username());
        if (!messageRepository.existsById(req.messageId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "消息不存在");
        }
        if (!messageDeletionRepository.existsByMessageIdAndUsername(req.messageId(), req.username())) {
            MessageDeletion d = new MessageDeletion();
            d.messageId = req.messageId();
            d.username = req.username();
            messageDeletionRepository.save(d);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        m.put("message", "已删除");
        return m;
    }

    // ============ 消息已读 / 未读数（红点） ============

    /**
     * POST /api/chat/read —— 将与某人的会话标记为已读。
     *
     * @param req 请求体：username 当前用户、partner 聊天对象
     * @return success=true 与 lastReadMessageId（已读到的最新消息 ID，无消息时为 0）
     */
    @PostMapping("/api/chat/read")
    public Map<String, Object> markRead(@RequestBody ReadRequest req) {
        relationService.validateUsers(req.username(), req.partner());
        // 取双方最新一条消息的 ID 作为已读位置
        long lastId = 0L;
        List<Message> msgs = messageRepository.findBetweenDesc(req.username(), req.partner());
        if (!msgs.isEmpty()) {
            lastId = msgs.get(0).id;
        }
        // 没有已读记录则新建，否则更新
        MessageRead mr = messageReadRepository.findByUsernameAndPartner(req.username(), req.partner()).orElse(null);
        if (mr == null) {
            mr = new MessageRead();
            mr.username = req.username();
            mr.partner = req.partner();
        }
        mr.lastReadMessageId = lastId;
        messageReadRepository.save(mr);
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        m.put("lastReadMessageId", lastId);
        return m;
    }

    /**
     * GET /api/chat/unread —— 获取各会话的未读消息数（红点）。
     *
     * @param username 当前用户名
     * @return 仅包含未读数大于 0 的已匹配对象：partnerUsername、unreadCount
     */
    @GetMapping("/api/chat/unread")
    public List<Map<String, Object>> getUnread(@RequestParam("username") String username) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            if (u.username.equals(username)) continue;
            if (!relationService.isMatched(username, u.username)) continue;
            // 统计对方发来、且 ID 大于上次已读位置的消息数
            long lastRead = messageReadRepository.findByUsernameAndPartner(username, u.username)
                    .map(mr -> mr.lastReadMessageId).orElse(0L);
            long count = messageRepository.countUnreadFrom(u.username, username, lastRead);
            if (count > 0) {
                Map<String, Object> m = new HashMap<>();
                m.put("partnerUsername", u.username);
                m.put("unreadCount", count);
                result.add(m);
            }
        }
        return result;
    }

    // ============ 聊天评分 ============

    /**
     * GET /api/chat/rating —— 查询用户对某聊天室的评分。
     *
     * @param roomId   聊天室 ID
     * @param username 评分用户名
     * @return {rating: 分数}，未评分时为 0
     */
    @GetMapping("/api/chat/rating")
    public Map<String, Object> getChatRating(@RequestParam("roomId") Long roomId,
                                             @RequestParam("username") String username) {
        ChatRating r = chatRatingRepository.findByRoomIdAndUsername(roomId, username).orElse(null);
        return Map.of("rating", r == null || r.score == null ? 0 : r.score);
    }

    /**
     * POST /api/chat/rating —— 提交或更新聊天室评分。
     *
     * @param body 请求体：roomId 聊天室 ID、username 用户名、score 分数（缺省为 0）
     * @return {success: true/false}；roomId 或 username 缺失时为 false
     * 业务规则：同一用户对同一聊天室只保留一条评分，已存在则覆盖。
     */
    @PostMapping("/api/chat/rating")
    public Map<String, Object> setChatRating(@RequestBody Map<String, Object> body) {
        Long roomId = body.get("roomId") == null ? null : Long.valueOf(body.get("roomId").toString());
        String username = str(body.get("username"));
        Integer score = body.get("score") == null ? 0 : Integer.valueOf(body.get("score").toString());
        if (roomId == null || username == null) return Map.of("success", false);
        ChatRating r = chatRatingRepository.findByRoomIdAndUsername(roomId, username).orElseGet(ChatRating::new);
        r.roomId = roomId;
        r.username = username;
        r.score = score;
        chatRatingRepository.save(r);
        return Map.of("success", true);
    }

    // ============ 聊天内 AI 帮手 ============

    /**
     * POST /api/chat/ai-help —— 聊天内 AI 帮手（调用 DeepSeek）。
     *
     * @param body 请求体：message 用户的问题、context 最近的对话（可选）、targetLang 回答语言（缺省中文）
     * @return {success, answer}；调用失败时 success=false 且 answer 为空字符串
     * 提示词要求：给出可直接发送的简短回答，并使用敬语。
     */
    @PostMapping("/api/chat/ai-help")
    public Map<String, Object> chatAiHelp(@RequestBody Map<String, Object> body) {
        String message = str(body.get("message"));
        String context = str(body.get("context"));
        String targetLang = str(body.get("targetLang"));
        try {
            // 系统提示词：限定助手角色、回答语言与语气
            String sys = "你是 SOUL 技能交换平台的对话助手，帮助用户组织语言、解释表达。只给出可直接发送的简短回答，使用"
                    + (targetLang == null ? "中文" : targetLang) + "，并使用敬语。";
            // 拼接用户提示词：有上下文则带上最近对话，再附上用户问题
            String user = (context == null ? "" : "最近的对话：\n" + context + "\n\n")
                    + "用户的问题：" + (message == null ? "" : message);
            // 调用 AI，最多返回 512 个 token
            String answer = deepSeek.chat(List.of(
                    Map.of("role", "system", "content", sys),
                    Map.of("role", "user", "content", user)), 512);
            return Map.of("success", true, "answer", answer);
        } catch (Exception e) {
            return Map.of("success", false, "answer", "");
        }
    }

}
