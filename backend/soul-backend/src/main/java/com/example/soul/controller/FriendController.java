package com.example.soul.controller;

import com.example.soul.service.RelationService;

import com.example.soul.entity.Block;
import com.example.soul.entity.Friend;
import com.example.soul.entity.FriendApplication;
import com.example.soul.entity.Message;
import com.example.soul.entity.Remark;
import com.example.soul.entity.User;
import com.example.soul.repository.BlockRepository;
import com.example.soul.repository.FriendApplicationRepository;
import com.example.soul.repository.FriendRepository;
import com.example.soul.repository.RemarkRepository;
import com.example.soul.repository.UserRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 好友 / 黑名单 / 备注 相关的 REST 接口控制器。
 * 包含：发送与处理好友申请、好友列表、删除好友、拉黑与解除拉黑、设置备注、查询两人关系状态。
 * 具体的关系判断与增删逻辑委托给 relationService；请求/响应 DTO（record）定义在父类 BaseApiController 中。
 */
@RestController
public class FriendController extends BaseApiController {

    // ============ 好友 / 黑名单 ============

    /**
     * POST /api/friend/request —— 发送好友申请。
     * 参数（请求体 FriendRequest）：username 申请人，friendUsername 被申请人，message 附言。
     * 返回：{success, message}。
     * 业务规则：先校验双方用户存在；已是好友则直接提示；已有 PENDING 申请则不重复创建；否则新建 PENDING 申请。
     */
    @PostMapping("/api/friend/request")
    public Map<String, Object> requestFriendEndpoint(@RequestBody FriendRequest req) {
        relationService.validateUsers(req.username(), req.friendUsername());
        Map<String, Object> m = new HashMap<>();
        // 已经是好友，无需再申请
        if (relationService.isFriend(req.username(), req.friendUsername())) {
            m.put("success", true);
            m.put("message", "已经是好友");
            return m;
        }
        // 同一方向已有待处理申请，避免重复提交
        if (friendApplicationRepository.existsByFromUserAndToUserAndStatus(req.username(), req.friendUsername(), "PENDING")) {
            m.put("success", true);
            m.put("message", "已发送过申请，等待对方处理");
            return m;
        }
        FriendApplication fa = new FriendApplication();
        fa.fromUser = req.username();
        fa.toUser = req.friendUsername();
        fa.message = req.message();
        fa.status = "PENDING";
        fa.createdAt = Instant.now().toString();
        friendApplicationRepository.save(fa);
        m.put("success", true);
        m.put("message", "好友申请已发送");
        return m;
    }

    /**
     * POST /api/friend/respond —— 同意或拒绝好友申请（事务内执行）。
     * 参数（请求体 FriendRespondRequest）：requestId 申请 ID，accept 是否同意。
     * 返回：{success}；申请不存在时 {success:false, message}。
     * 业务规则：更新申请状态为 ACCEPTED / REJECTED；同意时才建立好友关系。
     */
    @PostMapping("/api/friend/respond")
    @Transactional
    public Map<String, Object> respondFriendEndpoint(@RequestBody FriendRespondRequest req) {
        FriendApplication fa = friendApplicationRepository.findById(req.requestId()).orElse(null);
        Map<String, Object> m = new HashMap<>();
        if (fa == null) {
            m.put("success", false);
            m.put("message", "申请不存在");
            return m;
        }
        fa.status = req.accept() ? "ACCEPTED" : "REJECTED";
        friendApplicationRepository.save(fa);
        // 仅在同意时建立双向好友关系
        if (req.accept()) {
            relationService.addFriend(fa.fromUser, fa.toUser);
        }
        m.put("success", true);
        return m;
    }

    /**
     * GET /api/friend/requests?username= —— 查询发给该用户的待处理（PENDING）好友申请。
     * 返回：FriendRequestResponse 列表（申请 ID、申请人账号、申请人昵称、附言、创建时间）。
     */
    @GetMapping("/api/friend/requests")
    public List<FriendRequestResponse> listFriendRequests(@RequestParam("username") String username) {
        List<FriendRequestResponse> result = new ArrayList<>();
        for (FriendApplication fa : friendApplicationRepository.findByToUserAndStatus(username, "PENDING")) {
            // 找不到申请人资料时，昵称回退为账号名
            User from = userRepository.findByUsername(fa.fromUser).orElse(null);
            result.add(new FriendRequestResponse(fa.id, fa.fromUser, from == null ? fa.fromUser : from.name, fa.message, fa.createdAt));
        }
        return result;
    }

    /**
     * POST /api/friend/remove —— 删除好友（事务内执行）。
     * 参数（请求体 FriendRequest）：username 操作者，friendUsername 被删除的好友。
     * 返回：{success:true}。
     */
    @PostMapping("/api/friend/remove")
    @Transactional
    public Map<String, Object> removeFriendEndpoint(@RequestBody FriendRequest req) {
        relationService.removeFriend(req.username(), req.friendUsername());
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        return m;
    }

    /**
     * GET /api/friend/list?username= —— 获取好友列表。
     * 返回：MatchUserResponse 列表，其中昵称已按该用户设置的备注替换（applyRemark）。
     */
    @GetMapping("/api/friend/list")
    public List<MatchUserResponse> listFriends(@RequestParam("username") String username) {
        List<MatchUserResponse> result = new ArrayList<>();
        for (Friend f : friendRepository.findByUserAOrUserB(username, username)) {
            // 好友关系以 (userA, userB) 存储，取不是自己的那一方
            String other = f.userA.equals(username) ? f.userB : f.userA;
            userRepository.findByUsername(other).ifPresent(u -> result.add(applyRemark(username, toMatch(u))));
        }
        return result;
    }

    /**
     * POST /api/block/add —— 拉黑用户（事务内执行）。
     * 参数（请求体 BlockRequest）：username 拉黑者，blockUsername 被拉黑者。
     * 返回：{success:true}。先校验双方用户存在。
     */
    @PostMapping("/api/block/add")
    @Transactional
    public Map<String, Object> addBlockEndpoint(@RequestBody BlockRequest req) {
        relationService.validateUsers(req.username(), req.blockUsername());
        relationService.addBlock(req.username(), req.blockUsername());
        // 拉黑不删除好友：解除拉黑后若对方未删除，仍是好友
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        return m;
    }

    /**
     * POST /api/block/remove —— 解除拉黑（事务内执行）。
     * 参数（请求体 BlockRequest）：username 拉黑者，blockUsername 被拉黑者。
     * 返回：{success:true}。
     */
    @PostMapping("/api/block/remove")
    @Transactional
    public Map<String, Object> removeBlockEndpoint(@RequestBody BlockRequest req) {
        relationService.removeBlock(req.username(), req.blockUsername());
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        return m;
    }

    /**
     * GET /api/block/list?username= —— 获取该用户的黑名单。
     * 返回：被拉黑用户的 MatchUserResponse 列表（不应用备注）。
     */
    @GetMapping("/api/block/list")
    public List<MatchUserResponse> listBlocks(@RequestParam("username") String username) {
        List<MatchUserResponse> result = new ArrayList<>();
        for (Block b : blockRepository.findByBlocker(username)) {
            userRepository.findByUsername(b.blocked).ifPresent(u -> result.add(toMatch(u)));
        }
        return result;
    }

    /**
     * POST /api/remark/set —— 设置（新增或覆盖）对某用户的备注名。
     * 参数（请求体 RemarkRequest）：owner 备注者，target 被备注者，remark 备注内容（null 视为空串，并去除首尾空格）。
     * 返回：{success:true}。
     */
    @PostMapping("/api/remark/set")
    public Map<String, Object> setRemark(@RequestBody RemarkRequest req) {
        relationService.validateUsers(req.owner(), req.target());
        Remark r = remarkRepository.findByOwnerAndTarget(req.owner(), req.target()).orElse(null);
        // 已有备注则更新，否则新建
        if (r == null) {
            r = new Remark();
            r.owner = req.owner();
            r.target = req.target();
        }
        r.remark = req.remark() == null ? "" : req.remark().trim();
        remarkRepository.save(r);
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        return m;
    }

    /**
     * GET /api/relation/status?username=&other= —— 查询两个用户之间的关系。
     * 返回：RelationStatus，依次为：是否已匹配、是否为好友、username 是否拉黑了 other、other 是否拉黑了 username。
     */
    @GetMapping("/api/relation/status")
    public RelationStatus relationStatus(@RequestParam("username") String username,
                                         @RequestParam("other") String other) {
        return new RelationStatus(
                relationService.isMatched(username, other),
                relationService.isFriend(username, other),
                relationService.isBlocked(username, other),
                relationService.isBlocked(other, username));
    }

}
