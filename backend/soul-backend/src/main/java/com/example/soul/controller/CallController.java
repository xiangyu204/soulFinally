package com.example.soul.controller;

import com.example.soul.service.MessageService;
import com.example.soul.service.RelationService;

import com.example.soul.entity.User;
import com.example.soul.repository.UserRepository;
import com.example.soul.store.Store;

import io.agora.media.RtcTokenBuilder2;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 语音/视频通话信令控制器。
 * <p>
 * 负责通话的发起、接听/拒接、挂断、状态轮询、来电查询，以及 Agora RTC 临时 Token 的签发。
 * 通话状态流转：RINGING（响铃中）→ ACTIVE（通话中，被叫接听）→ ENDED（已结束）；
 * 或 RINGING → REJECTED（被叫拒接）。查询无来电时返回 "NONE"。
 * 所有通话数据保存在内存 {@link Store#calls} 中（key 为 callId），服务重启后会丢失。
 * 通话结束/拒接/取消时，会通过 MessageService 往聊天记录里追加一条通话消息。
 */
@RestController
public class CallController extends BaseApiController {

    // ============ 语音/视频通话信令 ============

    /**
     * POST /api/call/start —— 主叫发起通话。
     * 请求体 CallStartRequest：caller（主叫用户名）、callee（被叫用户名）、callType（"VIDEO" 为视频，其余一律按 "VOICE" 语音处理）。
     * 校验：双方用户存在、未互相拉黑、必须是好友，否则抛 403。
     * 状态：新建通话初始状态为 RINGING；若双方已有进行中（RINGING/ACTIVE）的通话，则直接返回该通话，不重复创建。
     * 返回：CallStatusResponse（含 callId、channelName 等，客户端用 channelName 加入 Agora 频道）。
     */
    @PostMapping("/api/call/start")
    public CallStatusResponse startCall(@RequestBody CallStartRequest req) {
        relationService.validateUsers(req.caller(), req.callee());
        if (relationService.isBlockedEither(req.caller(), req.callee())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "对方已把你拉黑");
        }
        if (!relationService.isFriend(req.caller(), req.callee())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "加为好友后才能通话");
        }
        // 频道名对双方一致（与谁是主叫无关），见 channelName()
        String channel = channelName(req.caller(), req.callee());
        // 幂等：已有进行中的通话就复用，避免重复响铃
        Store.Call existing = findActiveCall(req.caller(), req.callee());
        if (existing != null) {
            return toCallStatus(existing);
        }
        Store.Call call = new Store.Call();
        call.callId = UUID.randomUUID().toString();
        call.caller = req.caller();
        call.callee = req.callee();
        call.channelName = channel;
        // 只区分视频/语音两种类型，非 VIDEO 的任何值都按语音处理
        call.callType = "VIDEO".equalsIgnoreCase(req.callType()) ? "VIDEO" : "VOICE";
        call.status = "RINGING";
        call.updatedAt = System.currentTimeMillis();
        store.calls.put(call.callId, call);
        return toCallStatus(call);
    }

    /**
     * POST /api/call/answer —— 被叫接听或拒接。
     * 请求体 CallAnswerRequest：callId（通话 ID）、accept（true 接听 / false 拒接）。
     * 状态：accept=true → ACTIVE 并记录接听时间 answeredAt；accept=false → REJECTED，并追加一条 "missed"（未接听）通话消息。
     * 通话不存在返回 404。返回：更新后的 CallStatusResponse。
     */
    @PostMapping("/api/call/answer")
    public CallStatusResponse answerCall(@RequestBody CallAnswerRequest req) {
        Store.Call call = store.calls.get(req.callId());
        if (call == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "通话不存在");
        }
        call.status = req.accept() ? "ACTIVE" : "REJECTED";
        call.updatedAt = System.currentTimeMillis();
        if (req.accept()) {
            call.answeredAt = System.currentTimeMillis();
        } else {
            // 拒接 → 记录一条未接听
            messageService.appendCallMessage(call.caller, call.callee, "missed", call.callType);
        }
        return toCallStatus(call);
    }

    /**
     * POST /api/call/end —— 任意一方挂断通话。
     * 请求体 CallEndRequest：callId（通话 ID）。
     * 状态：统一置为 ENDED。若已接听（answeredAt &gt; 0），追加 "ended:通话秒数" 消息；
     * 若尚未接听（主叫取消），追加 "canceled" 消息。通话不存在返回 404。
     * 返回：更新后的 CallStatusResponse。
     */
    @PostMapping("/api/call/end")
    public CallStatusResponse endCall(@RequestBody CallEndRequest req) {
        Store.Call call = store.calls.get(req.callId());
        if (call == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "通话不存在");
        }
        call.status = "ENDED";
        call.updatedAt = System.currentTimeMillis();
        // answeredAt 大于 0 表示曾经接听过，时长 = 当前时间 - 接听时间（毫秒转秒）
        if (call.answeredAt > 0) {
            long durationSec = (System.currentTimeMillis() - call.answeredAt) / 1000L;
            messageService.appendCallMessage(call.caller, call.callee, "ended:" + durationSec, call.callType);
        } else {
            // 未接听就结束（主叫取消）
            messageService.appendCallMessage(call.caller, call.callee, "canceled", call.callType);
        }
        return toCallStatus(call);
    }

    /**
     * GET /api/call/status?callId=... —— 按 callId 查询通话当前状态（客户端轮询用，例如主叫等待对方接听）。
     * 返回：CallStatusResponse；通话不存在返回 404。
     */
    @GetMapping("/api/call/status")
    public CallStatusResponse callStatus(@RequestParam("callId") String callId) {
        Store.Call call = store.calls.get(callId);
        if (call == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "通话不存在");
        }
        return toCallStatus(call);
    }

    /**
     * GET /api/call/incoming?username=... —— 查询某用户是否有正在响铃（RINGING）的来电（客户端轮询用）。
     * 返回：有来电则返回该通话的 CallStatusResponse；没有则返回 status 为 "NONE"、其余字段为 null 的占位响应。
     */
    @GetMapping("/api/call/incoming")
    public CallStatusResponse incomingCall(@RequestParam("username") String username) {
        Store.Call call = findRingingFor(username);
        if (call == null) {
            return new CallStatusResponse(null, null, null, null, null, "NONE", null);
        }
        return toCallStatus(call);
    }

    /**
     * GET /api/call/token?channelName=...&amp;uid=... —— 为 Agora 频道签发 RTC 临时 Token。
     * 参数：channelName（频道名）、uid（Agora 用户数字 ID，默认 0，表示由 Agora 分配）。
     * 逻辑：使用服务端配置的 App ID 和 App Certificate，以发布者（ROLE_PUBLISHER，可推流）身份生成 Token，有效期 3600 秒。
     * 若 App ID / 证书未配置返回 500。返回：CallTokenResponse（token、appId、uid、channelName）。
     * 注意：App Certificate 属于敏感配置，只在服务端使用，不会返回给客户端。
     */
    @GetMapping("/api/call/token")
    public CallTokenResponse getCallToken(@RequestParam("channelName") String channelName,
                                          @RequestParam(value = "uid", defaultValue = "0") int uid) {
        // 未配置 Agora 凭据时无法签发 Token
        if (agoraAppId == null || agoraAppId.isBlank()
                || agoraAppCertificate == null || agoraAppCertificate.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Agora config missing");
        }
        // Token 与权限的有效期均为 3600 秒（1 小时）
        int expire = 3600;
        String token = new RtcTokenBuilder2().buildTokenWithUid(
                agoraAppId, agoraAppCertificate, channelName, uid,
                RtcTokenBuilder2.Role.ROLE_PUBLISHER, expire, expire);
        return new CallTokenResponse(token, agoraAppId, uid, channelName);
    }


    /** 查找两个用户之间进行中（RINGING 或 ACTIVE）的通话，没有则返回 null。 */
    private Store.Call findActiveCall(String a, String b) {
        String channel = channelName(a, b);
        for (Store.Call call : store.calls.values()) {
            if (call.channelName.equals(channel)
                    && ("RINGING".equals(call.status) || "ACTIVE".equals(call.status))) {
                return call;
            }
        }
        return null;
    }

    /** 查找以 username 为被叫、且正在响铃（RINGING）的通话，没有则返回 null。 */
    private Store.Call findRingingFor(String username) {
        for (Store.Call call : store.calls.values()) {
            if ("RINGING".equals(call.status) && call.callee.equals(username)) {
                return call;
            }
        }
        return null;
    }

    /** 生成双方共用的 Agora 频道名：按字典序排序后用 "|" 连接，保证 (a,b) 与 (b,a) 得到同一个频道。 */
    private String channelName(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    /** 把内存中的通话对象转换为响应 DTO；callerName 优先用主叫的昵称，没有昵称则退回用户名。 */
    private CallStatusResponse toCallStatus(Store.Call call) {
        User caller = userRepository.findByUsername(call.caller).orElse(null);
        String callerName = (caller != null && caller.name != null && !caller.name.isBlank())
                ? caller.name : call.caller;
        return new CallStatusResponse(call.callId, call.caller, call.callee,
                call.channelName, call.callType, call.status, callerName);
    }

}
