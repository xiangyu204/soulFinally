package com.example.soul.controller;

import com.example.soul.service.MessageService;
import com.example.soul.service.RelationService;

import com.example.soul.service.MessageService;
import com.example.soul.service.RelationService;

import com.example.soul.client.DeepSeekClient;
import com.example.soul.entity.Block;
import com.example.soul.entity.Friend;
import com.example.soul.entity.Match;
import com.example.soul.entity.Message;
import com.example.soul.entity.Remark;
import com.example.soul.entity.User;
import com.example.soul.repository.BlockRepository;
import com.example.soul.repository.BoardCommentRepository;
import com.example.soul.repository.BoardPostRepository;
import com.example.soul.repository.ChatRatingRepository;
import com.example.soul.repository.FriendApplicationRepository;
import com.example.soul.repository.FriendRepository;
import com.example.soul.repository.MatchRepository;
import com.example.soul.repository.MessageDeletionRepository;
import com.example.soul.repository.MessageReadRepository;
import com.example.soul.repository.MessageRepository;
import com.example.soul.repository.RemarkRepository;
import com.example.soul.repository.UserRepository;
import com.example.soul.store.Store;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 各 Controller 共用的依赖注入、DTO 与业务工具方法。
 * <p>
 * 这是所有业务 Controller 的公共抽象基类，作用有三：
 * <ul>
 *   <li>统一注入 Service、Repository、AI 客户端等依赖，子类可直接使用（protected 字段）；</li>
 *   <li>集中定义与 App 端交互的请求/响应 DTO（使用 Java record，字段名需与 App 端一致）；</li>
 *   <li>提供通用的实体转换、字符串处理、错误响应等辅助方法。</li>
 * </ul>
 */
public abstract class BaseApiController {

    /** 内存存储（运行时临时数据，例如通话、房间等状态） */
    @Autowired protected Store store;
    /** 消息业务服务（消息类型规范化等） */
    @Autowired protected MessageService messageService;
    /** 用户关系业务服务（匹配、好友、拉黑等关系判断） */
    @Autowired protected RelationService relationService;
    /** DeepSeek AI 客户端，用于 AI 指南、测验出题等功能 */
    @Autowired protected DeepSeekClient deepSeek;
    /** 用户数据访问 */
    @Autowired protected UserRepository userRepository;
    /** 聊天消息数据访问 */
    @Autowired protected MessageRepository messageRepository;
    /** 匹配记录数据访问 */
    @Autowired protected MatchRepository matchRepository;
    /** 好友关系数据访问 */
    @Autowired protected FriendRepository friendRepository;
    /** 拉黑记录数据访问 */
    @Autowired protected BlockRepository blockRepository;
    /** 好友申请数据访问 */
    @Autowired protected FriendApplicationRepository friendApplicationRepository;
    /** 好友备注数据访问 */
    @Autowired protected RemarkRepository remarkRepository;
    /** 消息删除记录（按用户“删除”消息）数据访问 */
    @Autowired protected MessageDeletionRepository messageDeletionRepository;
    /** 消息已读记录数据访问 */
    @Autowired protected MessageReadRepository messageReadRepository;
    /** 留言板帖子数据访问 */
    @Autowired protected BoardPostRepository boardPostRepository;
    /** 留言板评论数据访问 */
    @Autowired protected BoardCommentRepository boardCommentRepository;
    /** 聊天评分数据访问 */
    @Autowired protected ChatRatingRepository chatRatingRepository;
    /** JSON 序列化/反序列化工具（线程安全，可共享同一个实例） */
    protected final ObjectMapper mapper = new ObjectMapper();

    /** 上传文件（头像、聊天媒体等）的保存目录，来自配置 app.upload.dir，默认 uploads */
    @Value("${app.upload.dir:uploads}")
    protected String uploadDir;

    /** 声网 Agora 的 App ID（配置项 agora.app.id，未配置时为空字符串） */
    @Value("${agora.app.id:}")
    protected String agoraAppId;

    /** 声网 Agora 的 App 证书（配置项 agora.app.certificate，敏感信息，勿输出到日志；未配置时为空字符串） */
    @Value("${agora.app.certificate:}")
    protected String agoraAppCertificate;

    // ============ DTO（字段名与 App 端完全一致） ============

    /** 登录请求：username 用户名，password 密码 */
    public record LoginRequest(String username, String password) {}
    /** 登录响应：success 是否成功，name 昵称，username 用户名，email 邮箱，avatar 头像地址，role 角色，message 提示信息 */
    public record LoginResponse(boolean success, String name, String username, String email,
                                String avatar, String role, String message) {}
    /**
     * 用户资料响应：id 用户ID，username 用户名，name 昵称，email 邮箱，avatar 头像，gender 性别，age 年龄，
     * skillOffer 可提供的技能，skillWant 想学习的技能，skillOfferLevel/skillWantLevel 对应技能等级，
     * nationality 国籍，averageRating 平均评分，ratingCount 评分次数，role 角色，
     * phone 电话，address 地址，timeSlot 可用时间段
     */
    public record ProfileResponse(Long id, String username, String name, String email, String avatar,
                                  String gender, Integer age, String skillOffer, String skillWant,
                                  String skillOfferLevel, String skillWantLevel, String nationality,
                                  Double averageRating, Integer ratingCount, String role,
                                  String phone, String address, String timeSlot) {}
    /** 头像上传响应：success 是否成功，avatar 新头像地址，message 提示信息 */
    public record AvatarUploadResponse(boolean success, String avatar, String message) {}
    /** AI 指南响应：advice 为 AI 给出的建议文本 */
    public record AiGuideResponse(String advice) {}
    /**
     * 匹配用户响应（匹配列表中的单个用户）：基本资料同 ProfileResponse；
     * skillOffer/skillWant 已拆分为技能列表（见 {@link #split(String)}），timeSlot 可用时间段，
     * skillWantLevel/skillOfferLevel 技能等级，averageRating/ratingCount 评分信息
     */
    public record MatchUserResponse(Long id, String username, String name, Integer age, String gender,
                                    String nationality, String avatar, List<String> skillOffer,
                                    List<String> skillWant, String timeSlot, String skillWantLevel,
                                    String skillOfferLevel, Double averageRating, Integer ratingCount) {}
    /** 匹配历史项：partnerId 对方ID，partnerName 对方昵称，partnerAvatar 对方头像，matchTime 匹配时间 */
    public record MatchHistoryResponse(Long partnerId, String partnerName, String partnerAvatar, String matchTime) {}
    /**
     * 用户匹配详情响应：用户基本信息与评分（skillOffer/skillWant 为原始逗号分隔字符串），
     * histories 为该用户的匹配历史列表
     */
    public record UserMatchProfileResponse(Long id, String username, String name, Integer age,
                                           String nationality, String avatar, String skillOffer,
                                           String skillWant, Double averageRating, Integer ratingCount,
                                           List<MatchHistoryResponse> histories) {}
    /** 发送聊天消息请求：senderUsername 发送者，receiverUsername 接收者，content 内容，type 消息类型，mediaUrl 媒体文件地址 */
    public record ChatSendRequest(String senderUsername, String receiverUsername, String content,
                                  String type, String mediaUrl) {}
    /** 聊天消息（返回给 App）：id 消息ID，sender/receiver 收发双方用户名，content 内容，createdAt 创建时间，type 消息类型，mediaUrl 媒体地址，recalled 是否已撤回 */
    public record BackendChatMessage(Long id, String senderUsername, String receiverUsername,
                                     String content, String createdAt, String type, String mediaUrl, boolean recalled) {}
    /** 发送消息响应：success 是否成功，message 提示信息，data 已保存的消息 */
    public record ChatSendResponse(boolean success, String message, BackendChatMessage data) {}
    /** 消息操作请求（如撤回、删除）：messageId 消息ID，username 操作人用户名 */
    public record MessageActionRequest(Long messageId, String username) {}
    /** 标记已读请求：username 当前用户，partner 聊天对象 */
    public record ReadRequest(String username, String partner) {}
    /** 好友申请请求：username 申请人，friendUsername 被申请人，message 附言 */
    public record FriendRequest(String username, String friendUsername, String message) {}
    /** 处理好友申请请求：requestId 申请ID，username 处理人，accept 是否同意 */
    public record FriendRespondRequest(Long requestId, String username, boolean accept) {}
    /** 好友申请响应项：requestId 申请ID，fromUser 申请人用户名，fromName 申请人昵称，message 附言，createdAt 申请时间 */
    public record FriendRequestResponse(Long requestId, String fromUser, String fromName, String message, String createdAt) {}
    /** 拉黑请求：username 操作人，blockUsername 被拉黑的用户 */
    public record BlockRequest(String username, String blockUsername) {}
    /** 关系状态：matched 是否已匹配，friend 是否为好友，blockedByMe 是否被我拉黑，blockedByOther 是否被对方拉黑 */
    public record RelationStatus(boolean matched, boolean friend, boolean blockedByMe, boolean blockedByOther) {}
    /** 备注请求：owner 备注所有者，target 被备注的用户，remark 备注内容 */
    public record RemarkRequest(String owner, String target, String remark) {}
    /** 发起通话请求：caller 主叫，callee 被叫，callType 通话类型（如语音/视频） */
    public record CallStartRequest(String caller, String callee, String callType) {}
    /** 应答通话请求：callId 通话ID，callee 被叫，accept 是否接听 */
    public record CallAnswerRequest(String callId, String callee, boolean accept) {}
    /** 结束通话请求：callId 通话ID，caller 主叫 */
    public record CallEndRequest(String callId, String caller) {}
    /** 通话状态响应：callId 通话ID，caller 主叫，callee 被叫，channelName 声网频道名，callType 通话类型，status 当前状态，callerName 主叫昵称 */
    public record CallStatusResponse(String callId, String caller, String callee, String channelName,
                                     String callType, String status, String callerName) {}
    /** 通话令牌响应：token 声网令牌，appId 声网 App ID，uid 用户在频道内的数字ID，channelName 频道名 */
    public record CallTokenResponse(String token, String appId, int uid, String channelName) {}
    /** 创建房间请求：owner 房主，roomName 房间名称 */
    public record RoomCreateRequest(String owner, String roomName) {}
    /** 邀请入房请求：roomId 房间ID，inviter 邀请人，invitee 被邀请人 */
    public record RoomInviteRequest(String roomId, String inviter, String invitee) {}
    /** 响应房间邀请请求：roomId 房间ID，username 被邀请人，accept 是否接受 */
    public record RoomRespondRequest(String roomId, String username, boolean accept) {}
    /** 房间消息请求：roomId 房间ID，sender 发送者，content 消息内容 */
    public record RoomMessageRequest(String roomId, String sender, String content) {}
    /**
     * 房间状态响应：roomId 房间ID，roomName 房间名，owner 房主，channelName 声网频道名，
     * participants 参与者用户名列表，participantInfos 参与者详细信息，pendingInvitations 待响应的受邀用户
     */
    public record RoomStatusResponse(String roomId, String roomName, String owner, String channelName,
                                     List<String> participants, List<RoomParticipant> participantInfos,
                                     List<String> pendingInvitations) {}
    /** 房间参与者信息：username 用户名，name 昵称，avatar 头像 */
    public record RoomParticipant(String username, String name, String avatar) {}
    /** 房间消息响应：sender 发送者，content 内容，timestamp 时间戳 */
    public record RoomMessageResponse(String sender, String content, long timestamp) {}
    /** 开始测验请求：skill 测验技能，quizType 测验类型，targetLang 目标语言，username 答题用户 */
    public record QuizStartRequest(String skill, String quizType, String targetLang, String username) {}
    /** 测验题目：questionId 题目ID，question 题干，type 题型，options 选项列表 */
    public record QuestionResponse(Long questionId, String question, String type, List<String> options) {}
    /** 开始测验响应：quizId 测验ID，skill 技能，quizType 测验类型，questions 题目列表 */
    public record QuizStartResponse(Long quizId, String skill, String quizType, List<QuestionResponse> questions) {}
    /** 单题作答：questionId 题目ID，answer 用户答案 */
    public record QuizAnswerRequest(Long questionId, String answer) {}
    /** 提交测验请求：quizId 测验ID，username 答题用户，skill 技能，quizType 测验类型，answers 所有作答 */
    public record QuizSubmitRequest(Long quizId, String username, String skill, String quizType,
                                    List<QuizAnswerRequest> answers) {}
    /** 提交测验响应：score 得分，total 总题数，level 等级代码，levelText 等级文字描述 */
    public record QuizSubmitResponse(int score, int total, String level, String levelText) {}

    /**
     * 将任意对象转为字符串，null 保持为 null（不会变成 "null"）。
     *
     * @param o 任意对象，可为 null
     * @return o.toString()；o 为 null 时返回 null
     */
    protected static String str(Object o) {
        return o == null ? null : o.toString();
    }

    // ============ 内部工具 ============

    /**
     * 将用户实体转换为资料响应 DTO。
     *
     * @param u 用户实体
     * @return 包含用户完整资料的 ProfileResponse
     */
    protected ProfileResponse toProfile(User u) {
        return new ProfileResponse(u.id, u.username, u.name, u.email, u.avatar, u.gender, u.age,
                u.skillOffer, u.skillWant, u.skillOfferLevel, u.skillWantLevel, u.nationality,
                u.averageRating, u.ratingCount, u.role, u.phone, u.address, u.timeSlot);
    }

    /**
     * 将用户实体转换为匹配列表用的 DTO，技能字段会从逗号分隔字符串拆成列表。
     *
     * @param u 用户实体
     * @return MatchUserResponse
     */
    protected MatchUserResponse toMatch(User u) {
        return new MatchUserResponse(u.id, u.username, u.name, u.age, u.gender, u.nationality, u.avatar,
                split(u.skillOffer), split(u.skillWant), u.timeSlot, u.skillWantLevel, u.skillOfferLevel,
                u.averageRating, u.ratingCount);
    }

    /**
     * 若 owner 给该用户设置了备注，则用备注替换显示的昵称。
     *
     * @param owner 备注所有者的用户名
     * @param m     原匹配用户响应
     * @return 有备注时返回昵称被替换为备注的新对象；无备注时原样返回 m
     */
    protected MatchUserResponse applyRemark(String owner, MatchUserResponse m) {
        String remark = remarkRepository.findByOwnerAndTarget(owner, m.username())
                .map(r -> r.remark).orElse("");
        // 没有备注则不做任何修改
        if (remark.isBlank()) return m;
        // record 不可变，因此只能新建对象，仅把 name 字段替换为备注
        return new MatchUserResponse(m.id(), m.username(), remark, m.age(), m.gender(), m.nationality(), m.avatar(),
                m.skillOffer(), m.skillWant(), m.timeSlot(), m.skillWantLevel(), m.skillOfferLevel(),
                m.averageRating(), m.ratingCount());
    }

    /**
     * 按英文逗号拆分字符串，去除首尾空白并忽略空项。
     *
     * @param s 逗号分隔的字符串，可为 null 或空白
     * @return 拆分后的列表；输入为 null/空白时返回空列表（可修改）
     */
    protected static List<String> split(String s) {
        if (s == null || s.isBlank()) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (String p : s.split(",")) {
            if (!p.trim().isEmpty()) list.add(p.trim());
        }
        return list;
    }

    /**
     * 判断逗号分隔的字符串中是否有某一项包含关键字（忽略大小写和首尾空白，为子串匹配）。
     *
     * @param csv     逗号分隔的字符串，可为 null
     * @param keyword 要查找的关键字
     * @return 任一项包含关键字返回 true；csv 为 null 或无匹配返回 false
     */
    protected static boolean containsIgnoreCase(String csv, String keyword) {
        if (csv == null) return false;
        for (String p : csv.toLowerCase().split(",")) {
            if (p.trim().contains(keyword.toLowerCase().trim())) return true;
        }
        return false;
    }

    /**
     * 将消息实体转换为返回给 App 的聊天消息 DTO。
     *
     * @param message 消息实体
     * @return BackendChatMessage，其中消息类型经 messageService 规范化
     */
    protected BackendChatMessage toChatMessage(Message message) {
        return new BackendChatMessage(message.id, message.senderUsername, message.receiverUsername,
                message.content, message.createdAt, messageService.normalizeMessageType(message.type), message.mediaUrl, message.recalled);
    }


    /**
     * 构造统一的失败响应体：{"success": false, "message": msg}。
     *
     * @param msg 错误提示信息
     * @return 可直接作为响应返回的 Map
     */
    protected static Map<String, Object> errorMap(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", false);
        m.put("message", msg);
        return m;
    }

}
