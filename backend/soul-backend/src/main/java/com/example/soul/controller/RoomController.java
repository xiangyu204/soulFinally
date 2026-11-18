package com.example.soul.controller;

import com.example.soul.service.RelationService;

import com.example.soul.entity.Message;
import com.example.soul.entity.User;
import com.example.soul.repository.UserRepository;
import com.example.soul.store.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 语聊房控制器（多人语音 + 文字聊天）。
 * <p>
 * 提供创建房间、邀请好友、响应邀请、查询房间状态、查询收到的邀请、离开房间、发送/拉取房间文字消息等接口。
 * 房间、参与者、邀请、房间消息全部保存在内存 {@link Store#rooms} 中（key 为 roomId），服务重启后会丢失。
 * 房间人数（已加入 + 待响应邀请）上限为 8 人；最后一个人离开后房间会被自动删除。
 * 邀请状态目前只有 "PENDING"（待响应）；被接受或拒绝后邀请记录会被移除。
 * 语音部分由客户端加入 Agora 频道（频道名见 toRoomStatus 返回的 "soul_room_" + roomId）完成，本控制器不签发 Token。
 */
@RestController
public class RoomController extends BaseApiController {

    // ============ 语聊房（多人语音 + 文字） ============

    /**
     * POST /api/room/create —— 创建语聊房。
     * 请求体 RoomCreateRequest：owner（房主用户名，必填）、roomName（房间名，为空时默认 "语聊房"）。
     * 逻辑：生成随机 roomId，房主自动成为第一个参与者，并存入内存 Store。
     * 返回：RoomStatusResponse（房间当前状态）；owner 为空返回 400。
     */
    @PostMapping("/api/room/create")
    public RoomStatusResponse createRoom(@RequestBody RoomCreateRequest req) {
        if (req.owner() == null || req.owner().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "创建者不能为空");
        }
        Store.Room room = new Store.Room();
        room.roomId = UUID.randomUUID().toString();
        room.roomName = (req.roomName() == null || req.roomName().isBlank()) ? "语聊房" : req.roomName();
        room.owner = req.owner();
        room.participants.add(req.owner());
        room.createdAt = System.currentTimeMillis();
        store.rooms.put(room.roomId, room);
        return toRoomStatus(room);
    }

    /**
     * POST /api/room/invite —— 邀请好友加入房间。
     * 请求体 RoomInviteRequest：roomId、inviter（邀请人）、invitee（被邀请人）。
     * 校验：房间存在（否则 404）；invitee 非空（400）；双方必须是好友（403）；
     * 已加入人数 + 待响应邀请数 不能达到 8 人（403）。
     * 逻辑：在房间的邀请表里记录 invitee → "PENDING"。返回：RoomStatusResponse。
     */
    @PostMapping("/api/room/invite")
    public RoomStatusResponse inviteToRoom(@RequestBody RoomInviteRequest req) {
        Store.Room room = store.rooms.get(req.roomId());
        if (room == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "房间不存在");
        if (req.invitee() == null || req.invitee().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "被邀请人不能为空");
        }
        if (!relationService.isFriend(req.inviter(), req.invitee())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能邀请好友");
        }
        if (room.participants.size() + room.invitations.size() >= 8) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "房间人数已达上限（8人）");
        }
        room.invitations.put(req.invitee(), "PENDING");
        return toRoomStatus(room);
    }

    /**
     * POST /api/room/respond —— 被邀请人接受或拒绝邀请。
     * 请求体 RoomRespondRequest：roomId、username（响应者）、accept（true 接受 / false 拒绝）。
     * 逻辑：无论接受与否都先移除该用户的邀请；接受时若房间已满 8 人返回 403，否则加入参与者（已在房间内则不重复加入）。
     * 返回：RoomStatusResponse；房间不存在返回 404。
     */
    @PostMapping("/api/room/respond")
    public RoomStatusResponse respondToInvite(@RequestBody RoomRespondRequest req) {
        Store.Room room = store.rooms.get(req.roomId());
        if (room == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "房间不存在");
        room.invitations.remove(req.username());
        if (req.accept()) {
            if (room.participants.size() >= 8) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "房间人数已达上限（8人）");
            }
            if (!room.participants.contains(req.username())) {
                room.participants.add(req.username());
            }
        }
        return toRoomStatus(room);
    }

    /**
     * GET /api/room/status?roomId=... —— 查询房间当前状态（客户端轮询用，刷新参与者列表）。
     * 返回：RoomStatusResponse；房间不存在返回 404。
     */
    @GetMapping("/api/room/status")
    public RoomStatusResponse roomStatus(@RequestParam("roomId") String roomId) {
        Store.Room room = store.rooms.get(roomId);
        if (room == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "房间不存在");
        return toRoomStatus(room);
    }

    /**
     * GET /api/room/incoming?username=... —— 查询某用户收到的、尚未响应（PENDING）的房间邀请。
     * 逻辑：遍历所有房间，找出邀请表中该用户状态为 "PENDING" 的房间。
     * 返回：RoomStatusResponse 列表，没有邀请时为空列表。
     */
    @GetMapping("/api/room/incoming")
    public List<RoomStatusResponse> incomingRooms(@RequestParam("username") String username) {
        List<RoomStatusResponse> result = new ArrayList<>();
        for (Store.Room room : store.rooms.values()) {
            if ("PENDING".equals(room.invitations.get(username))) {
                result.add(toRoomStatus(room));
            }
        }
        return result;
    }

    /**
     * POST /api/room/leave —— 离开房间。
     * 请求体 RoomRespondRequest：roomId、username（离开者；accept 字段在此接口中不使用）。
     * 逻辑：从参与者和邀请表中移除该用户；若房间已没有任何参与者，则从内存 Store 中删除整个房间。
     * 返回：{"success": true}；房间不存在返回 404。
     */
    @PostMapping("/api/room/leave")
    public Map<String, Object> leaveRoom(@RequestBody RoomRespondRequest req) {
        Store.Room room = store.rooms.get(req.roomId());
        if (room == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "房间不存在");
        room.participants.remove(req.username());
        room.invitations.remove(req.username());
        if (room.participants.isEmpty()) {
            store.rooms.remove(room.roomId);
        }
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        return m;
    }

    /**
     * POST /api/room/message —— 在房间内发送一条文字消息。
     * 请求体 RoomMessageRequest：roomId、sender（发送者）、content（消息内容，会去掉首尾空白）。
     * 校验：房间存在（404）；发送者必须在房间内（403）；内容不能为空（400）。
     * 逻辑：消息追加到房间的内存消息列表，带当前时间戳。返回：{"success": true}。
     */
    @PostMapping("/api/room/message")
    public Map<String, Object> sendRoomMessage(@RequestBody RoomMessageRequest req) {
        Store.Room room = store.rooms.get(req.roomId());
        if (room == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "房间不存在");
        if (!room.participants.contains(req.sender())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "你不在这个房间");
        }
        String content = req.content() == null ? "" : req.content().trim();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "消息不能为空");
        }
        Store.RoomMessage msg = new Store.RoomMessage();
        msg.sender = req.sender();
        msg.content = content;
        msg.timestamp = System.currentTimeMillis();
        room.messages.add(msg);
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        return m;
    }

    /**
     * GET /api/room/messages?roomId=... —— 拉取房间内全部文字消息（按发送顺序）。
     * 返回：RoomMessageResponse（sender、content、timestamp）列表；房间不存在时返回空列表而不是 404。
     */
    @GetMapping("/api/room/messages")
    public List<RoomMessageResponse> roomMessages(@RequestParam("roomId") String roomId) {
        Store.Room room = store.rooms.get(roomId);
        if (room == null) return new ArrayList<>();
        List<RoomMessageResponse> result = new ArrayList<>();
        for (Store.RoomMessage msg : room.messages) {
            result.add(new RoomMessageResponse(msg.sender, msg.content, msg.timestamp));
        }
        return result;
    }

    /**
     * 把内存中的房间对象转换为响应 DTO：
     * 待响应邀请列表（pending）、参与者详情（用户名、昵称、头像）、以及 Agora 频道名 "soul_room_" + roomId。
     */
    private RoomStatusResponse toRoomStatus(Store.Room room) {
        // 只收集状态仍为 PENDING 的被邀请人
        List<String> pending = new ArrayList<>();
        for (Map.Entry<String, String> e : room.invitations.entrySet()) {
            if ("PENDING".equals(e.getValue())) pending.add(e.getKey());
        }
        List<RoomParticipant> infos = new ArrayList<>();
        for (String username : room.participants) {
            // 昵称为空或用户查不到时，退回显示用户名；头像查不到则为 null
            User u = userRepository.findByUsername(username).orElse(null);
            infos.add(new RoomParticipant(username,
                    (u != null && u.name != null && !u.name.isBlank()) ? u.name : username,
                    u != null ? u.avatar : null));
        }
        return new RoomStatusResponse(room.roomId, room.roomName, room.owner,
                "soul_room_" + room.roomId, room.participants, infos, pending);
    }

}
