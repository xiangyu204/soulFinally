package com.example.soul.controller;

import com.example.soul.entity.BoardComment;
import com.example.soul.entity.BoardPost;
import com.example.soul.entity.Message;
import com.example.soul.entity.User;
import com.example.soul.repository.BoardCommentRepository;
import com.example.soul.repository.BoardPostRepository;
import com.example.soul.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 公告板（帖子）与评论相关的 REST 接口控制器，供 Web 前端使用。
 * 包含：帖子的列表 / 详情 / 发布 / 删除，以及评论的列表 / 发表 / 删除。
 * 帖子与评论的实体分别为 BoardPost、BoardComment；本控制器不使用 DTO record，请求体直接用 Map 接收。
 */
@RestController
public class BoardController extends BaseApiController {

    // ============ 公告板（Web 前端） ============

    /**
     * GET /api/board —— 获取全部帖子，按 id 倒序（最新的在前）。
     * 返回：BoardPost 列表。
     */
    @GetMapping("/api/board")
    public List<BoardPost> listBoard() {
        List<BoardPost> posts = boardPostRepository.findAll();
        // id 为空按 0 处理，降序排列
        posts.sort((a, b) -> {
            long ai = a.id == null ? 0 : a.id;
            long bi = b.id == null ? 0 : b.id;
            return Long.compare(bi, ai);
        });
        return posts;
    }

    /**
     * GET /api/board/{id} —— 获取单个帖子详情。
     * 参数：id 帖子 ID（路径变量）。
     * 返回：BoardPost；不存在时返回 404（"post not found"）。
     */
    @GetMapping("/api/board/{id}")
    public BoardPost getBoardPost(@PathVariable("id") Long id) {
        return boardPostRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));
    }

    /**
     * POST /api/board —— 发布帖子。
     * 请求体（Map）：username 作者账号，title 标题，content 内容，category 分类。
     * 返回：保存后的 BoardPost。
     * 业务规则：日期取服务器当天；点赞数、评论数初始为 0；若作者存在则冗余保存其昵称与头像。
     */
    @PostMapping("/api/board")
    public BoardPost createBoardPost(@RequestBody Map<String, Object> body) {
        BoardPost p = new BoardPost();
        p.username = str(body.get("username"));
        p.title = str(body.get("title"));
        p.content = str(body.get("content"));
        p.category = str(body.get("category"));
        p.date = LocalDate.now().toString();
        p.likes = 0;
        p.commentCount = 0;
        // 把作者昵称/头像冗余到帖子上，列表展示时无需再查用户表
        if (p.username != null) {
            User u = userRepository.findByUsername(p.username).orElse(null);
            if (u != null) {
                p.name = u.name;
                p.avatar = u.avatar;
            }
        }
        return boardPostRepository.save(p);
    }

    /**
     * DELETE /api/board/{id} —— 删除帖子。
     * 参数：id 帖子 ID（路径变量）；username 操作者账号（可选）。
     * 返回：{success:true}；帖子不存在返回 {success:false, message:"not found"}；
     *      操作者不是作者返回 {success:false, message:"forbidden"}。
     * 业务规则：仅当传入 username 且帖子有作者时才校验作者身份；删除帖子前先删除其全部评论。
     */
    @DeleteMapping("/api/board/{id}")
    public Map<String, Object> deleteBoardPost(@PathVariable("id") Long id,
                                               @RequestParam(value = "username", required = false) String username) {
        BoardPost p = boardPostRepository.findById(id).orElse(null);
        if (p == null) return Map.of("success", false, "message", "not found");
        // 权限校验：非作者不可删除
        if (username != null && p.username != null && !username.equals(p.username)) {
            return Map.of("success", false, "message", "forbidden");
        }
        // 先级联删除该帖的所有评论
        boardCommentRepository.deleteAll(boardCommentRepository.findByPostIdOrderByIdAsc(id));
        boardPostRepository.delete(p);
        return Map.of("success", true);
    }

    // ============ 公告板评论 ============

    /**
     * GET /api/comments/{postId} —— 获取某帖子的全部评论，按 id 升序（先发的在前）。
     * 参数：postId 帖子 ID（路径变量）。
     */
    @GetMapping("/api/comments/{postId}")
    public List<BoardComment> listComments(@PathVariable("postId") Long postId) {
        return boardCommentRepository.findByPostIdOrderByIdAsc(postId);
    }

    /**
     * POST /api/comments/{postId} —— 给帖子添加评论。
     * 参数：postId 帖子 ID（路径变量）；请求体（Map）：username 评论者账号，author 显示名，content 内容。
     * 返回：保存后的 BoardComment。
     * 业务规则：保存评论后重新统计该帖评论数并回写到帖子的 commentCount。
     */
    @PostMapping("/api/comments/{postId}")
    public BoardComment addComment(@PathVariable("postId") Long postId,
                                   @RequestBody Map<String, Object> body) {
        BoardComment c = new BoardComment();
        c.postId = postId;
        c.username = str(body.get("username"));
        c.author = str(body.get("author"));
        c.content = str(body.get("content"));
        c.date = LocalDate.now().toString();
        BoardComment saved = boardCommentRepository.save(c);
        // 同步帖子的评论数（以数据库实际数量为准）
        BoardPost p = boardPostRepository.findById(postId).orElse(null);
        if (p != null) {
            p.commentCount = (int) boardCommentRepository.countByPostId(postId);
            boardPostRepository.save(p);
        }
        return saved;
    }

    /**
     * DELETE /api/comments/{commentId} —— 删除评论。
     * 参数：commentId 评论 ID（路径变量）；username 操作者账号（可选）。
     * 返回：{success:true}；评论不存在返回 {success:false, message:"not found"}；
     *      操作者不是评论者返回 {success:false, message:"forbidden"}。
     * 业务规则：仅当传入 username 且评论有作者时才校验身份；删除后重新统计所属帖子的评论数。
     */
    @DeleteMapping("/api/comments/{commentId}")
    public Map<String, Object> deleteComment(@PathVariable("commentId") Long commentId,
                                             @RequestParam(value = "username", required = false) String username) {
        BoardComment c = boardCommentRepository.findById(commentId).orElse(null);
        if (c == null) return Map.of("success", false, "message", "not found");
        // 权限校验：非评论者不可删除
        if (username != null && c.username != null && !username.equals(c.username)) {
            return Map.of("success", false, "message", "forbidden");
        }
        boardCommentRepository.delete(c);
        // 同步所属帖子的评论数
        BoardPost p = boardPostRepository.findById(c.postId).orElse(null);
        if (p != null) {
            p.commentCount = (int) boardCommentRepository.countByPostId(c.postId);
            boardPostRepository.save(p);
        }
        return Map.of("success", true);
    }

}
