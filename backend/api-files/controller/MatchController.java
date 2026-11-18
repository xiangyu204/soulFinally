package com.example.soul.controller;

import com.example.soul.service.RelationService;

import com.example.soul.entity.Match;
import com.example.soul.entity.User;
import com.example.soul.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 匹配控制器：提供匹配对象的资料查询、候选匹配列表、已匹配用户列表以及全库用户搜索。
 * 匹配关系的判断委托给 RelationService。
 */
@RestController
public class MatchController extends BaseApiController {

    // ============ 匹配 ============

    /**
     * GET /api/match/profile/{userId} —— 查询指定用户的匹配资料。
     *
     * @param userId 用户 ID（路径参数）
     * @return UserMatchProfileResponse（基本信息、技能、评分；评论列表目前为空列表）
     * 业务规则：用户不存在时抛出异常。
     */
    @GetMapping("/api/match/profile/{userId}")
    public UserMatchProfileResponse getMatchProfile(@PathVariable("userId") Long userId) {
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");
        return new UserMatchProfileResponse(u.id, u.username, u.name, u.age, u.nationality, u.avatar,
                u.skillOffer, u.skillWant, u.averageRating, u.ratingCount, new ArrayList<>());
    }

    /**
     * GET /api/match —— 获取候选匹配用户列表。
     *
     * @param username 当前用户名（可选），用于排除自己和已匹配的用户
     * @param haveSkill 对方“可教授技能”需包含的关键字（可选，忽略大小写）
     * @param wantSkill 对方“想学习技能”需包含的关键字（可选，忽略大小写）
     * @param timeSlot 时间段（可选，当前逻辑中未参与过滤）
     * @param skillWantLevel 想学技能等级（可选，当前逻辑中未参与过滤）
     * @param skillOfferLevel 可教技能等级（可选，当前逻辑中未参与过滤）
     * @param limit 最多返回条数，默认 12
     * @return MatchUserResponse 列表
     */
    @GetMapping("/api/match")
    public List<MatchUserResponse> getMatches(@RequestParam(value = "username", required = false) String username,
                                              @RequestParam(value = "haveSkill", required = false) String haveSkill,
                                              @RequestParam(value = "wantSkill", required = false) String wantSkill,
                                              @RequestParam(value = "timeSlot", required = false) String timeSlot,
                                              @RequestParam(value = "skillWantLevel", required = false) String skillWantLevel,
                                              @RequestParam(value = "skillOfferLevel", required = false) String skillOfferLevel,
                                              @RequestParam(value = "limit", defaultValue = "12") int limit) {
        List<MatchUserResponse> result = new ArrayList<>();
        // 用户名为空白时视为未登录，不做排除
        String me = (username == null || username.isBlank()) ? null : username;
        for (User u : userRepository.findAll()) {
            // 跳过自己
            // 跳过已经匹配成功的用户
            // 按技能关键字过滤，未提供关键字则不过滤
            if (me != null && u.username.equals(me)) continue;
            if (me != null && relationService.isMatched(me, u.username)) continue;
            if (haveSkill != null && !haveSkill.isBlank() && !containsIgnoreCase(u.skillOffer, haveSkill)) continue;
            if (wantSkill != null && !wantSkill.isBlank() && !containsIgnoreCase(u.skillWant, wantSkill)) continue;
            result.add(toMatch(u));
            // 达到 limit 后停止
            if (result.size() >= limit) break;
        }
        return result;
    }

    /**
     * GET /api/match/mine —— 获取当前用户已匹配成功的用户列表。
     *
     * @param username 当前用户名
     * @return 与该用户已匹配的 MatchUserResponse 列表（不含自己）
     */
    @GetMapping("/api/match/mine")
    public List<MatchUserResponse> getMyMatches(@RequestParam("username") String username) {
        List<MatchUserResponse> result = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            if (u.username.equals(username)) continue;
            if (relationService.isMatched(username, u.username)) {
                result.add(toMatch(u));
            }
        }
        return result;
    }

    // ============ 全库搜索（名字 / 用户名 / 技能模糊匹配） ============

    /**
     * GET /api/search/users —— 全库用户搜索。
     *
     * @param username 当前用户名（传给仓库查询，用于排除自己）
     * @param q 搜索关键字（名字/用户名/技能模糊匹配）
     * @param limit 最多返回条数，默认 50
     * @return MatchUserResponse 列表；关键字为空时返回空列表
     */
    @GetMapping("/api/search/users")
    public List<MatchUserResponse> searchUsers(@RequestParam("username") String username,
                                               @RequestParam("q") String q,
                                               @RequestParam(value = "limit", defaultValue = "50") int limit) {
        List<MatchUserResponse> result = new ArrayList<>();
        if (q == null || q.isBlank()) {
            return result;
        }
        for (User u : userRepository.searchUsers(username, q.trim())) {
            result.add(toMatch(u));
            if (result.size() >= limit) break;
        }
        return result;
    }

}
